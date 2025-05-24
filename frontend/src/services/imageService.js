import api from './api';

const imageCache = new Map();

export const imageService = {
  getImageUrl: (imageName) => {
    if (!imageName) return 'https://via.placeholder.com/400';
    
    // Return cached URL if available
    if (imageCache.has(imageName)) {
      return imageCache.get(imageName);
    }

    // Create and cache the URL
    const imageUrl = `${api.defaults.baseURL}/images/${imageName}`;
    imageCache.set(imageName, imageUrl);
    return imageUrl;
  },

  uploadImage: async (file) => {
    const formData = new FormData();
    formData.append('image', file);
    
    const response = await api.post('/images/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  deleteImage: async (imageName) => {
    // Remove from cache if exists
    imageCache.delete(imageName);
    const response = await api.delete(`/images/${imageName}`);
    return response.data;
  },

  clearCache: () => {
    imageCache.clear();
  }
}; 