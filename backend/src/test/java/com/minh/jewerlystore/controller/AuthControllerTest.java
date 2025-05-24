package com.minh.jewerlystore.controller;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.repository.UserRepository;
import com.minh.jewerlystore.security.jwt.JwtUtils;
import com.minh.jewerlystore.security.jwt.LoginRequest;
import com.minh.jewerlystore.security.request.SignupRequest;
import com.minh.jewerlystore.security.response.MessageResponse;
import com.minh.jewerlystore.security.response.UserInfoResponse;
import com.minh.jewerlystore.security.services.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private User testUser;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Setup signup request
        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("encodedPassword");

        // Setup UserDetails
        userDetails = new UserDetailsImpl(
            1L,
            "testuser",
            "testuser@example.com",
            "encodedPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Setup Authentication mock
        authentication = mock(Authentication.class);
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtCookie(any(UserDetailsImpl.class)))
            .thenReturn(ResponseCookie.from("jwt", "token").build());

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().get(HttpHeaders.SET_COOKIE));
        assertTrue(response.getBody() instanceof UserInfoResponse);
        
        UserInfoResponse userInfoResponse = (UserInfoResponse) response.getBody();
        assertEquals(userDetails.getId(), userInfoResponse.getId());
        assertEquals(userDetails.getUsername(), userInfoResponse.getUsername());
        
        // Verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtCookie(any(UserDetailsImpl.class));
    }

    @Test
    void authenticateUser_Failure() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new AuthenticationException("Bad credentials") {});

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // Verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateJwtCookie(any(UserDetailsImpl.class));
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(encoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        assertEquals("User registered successfully!", ((MessageResponse) response.getBody()).getMessage());

        // Verify
        verify(userRepository).existsByUsername(signupRequest.getUsername());
        verify(userRepository).existsByEmail(signupRequest.getEmail());
        verify(encoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        assertEquals("Error: Username is already taken!", ((MessageResponse) response.getBody()).getMessage());

        // Verify
        verify(userRepository).existsByUsername(signupRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponse);
        assertEquals("Error: Email is already in use!", ((MessageResponse) response.getBody()).getMessage());

        // Verify
        verify(userRepository).existsByUsername(signupRequest.getUsername());
        verify(userRepository).existsByEmail(signupRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void currentUserName_Success() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");

        // Act
        String username = authController.currentUserName(authentication);

        // Assert
        assertEquals("testuser", username);
        
        // Verify
        verify(authentication).getName();
    }

    @Test
    void currentUserName_NoAuthentication() {
        // Act
        String username = authController.currentUserName(null);

        // Assert
        assertEquals("", username);
    }

    @Test
    void getUserDetails_Success() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        ResponseEntity<?> response = authController.getUserDetails(authentication);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof UserInfoResponse);
        
        UserInfoResponse userInfoResponse = (UserInfoResponse) response.getBody();
        assertEquals(userDetails.getId(), userInfoResponse.getId());
        assertEquals(userDetails.getUsername(), userInfoResponse.getUsername());
        assertEquals(1, userInfoResponse.getRoles().size());
        assertEquals("ROLE_USER", userInfoResponse.getRoles().get(0));

        // Verify
        verify(authentication).getPrincipal();
    }

    @Test
    void signoutUser_Success() {
        // Arrange
        ResponseCookie cookie = ResponseCookie.from("jwt", "").build();
        when(jwtUtils.getCleanJwtCookie()).thenReturn(cookie);

        // Act
        ResponseEntity<?> response = authController.signoutUser();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().get(HttpHeaders.SET_COOKIE));
        assertTrue(response.getBody() instanceof MessageResponse);
        assertEquals("You've been signed out!", ((MessageResponse) response.getBody()).getMessage());

        // Verify
        verify(jwtUtils).getCleanJwtCookie();
    }
} 