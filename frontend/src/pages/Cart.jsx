import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../services/cartService';
import { orderService } from '../services/orderService';
import { imageService } from '../services/imageService';

function Cart() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchCart();
  }, []);

  const fetchCart = async () => {
    try {
      setLoading(true);
      const data = await cartService.getCart();
      setCart(data);
      setError('');
    } catch (err) {
      setError('Failed to fetch cart, Login to continue');
      console.error('Error fetching cart:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateQuantity = async (productId, quantity) => {
    try {
      await cartService.updateCartItem(productId, quantity);
      fetchCart();
    } catch (err) {
      setError('Failed to update quantity');
      console.error('Error updating quantity:', err);
    }
  };

  const handleRemoveItem = async (productId) => {
    try {
      await cartService.removeFromCart(productId);
      fetchCart();
    } catch (err) {
      setError('Failed to remove item');
      console.error('Error removing item:', err);
    }
  };

  const handleCheckout = async () => {
    try {
      const orderData = {
        items: cart.items,
        totalAmount: calculateTotalPrice(),
      };
      await orderService.createOrder(orderData);
      await cartService.clearCart();
      navigate('/orders');
    } catch (err) {
      setError('Failed to create order');
      console.error('Error creating order:', err);
    }
  };

  // Calculate total price
  const calculateTotalPrice = () => {
    if (!cart?.items?.length) return 0;
    return cart.items.reduce((total, item) => {
      const price = item.productPrice || 0;
      return total + (price * item.quantity);
    }, 0);
  };

  // Memoize the total price
  const totalPrice = useMemo(() => calculateTotalPrice(), [cart?.items]);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-900">Shopping Cart</h2>

      {error ? (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      ) :

      cart?.items?.length > 0 ? (
        <div className="bg-white rounded-lg shadow-md">
          <div className="p-6 space-y-4">
            {cart.items.map((item) => (
              <div
                key={item.id}
                className="flex items-center justify-between border-b pb-4"
              >
                <div className="flex items-center space-x-4">
                  <img
                    src={`http://localhost:8080/api/images/${item.imageUrl}`}
                    alt={item.productName || 'Product'}
                    className="w-20 h-20 object-cover rounded"
                  />
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">
                      {item.productName || 'Product Name Not Available'}
                    </h3>
                    <p className="text-gray-600">${item.productPrice || 0}</p>
                    <p className="text-sm text-gray-500">
                      Subtotal: ${((item.productPrice || 0) * item.quantity).toFixed(2)}
                    </p>
                  </div>
                </div>

                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)}
                      className="text-gray-500 hover:text-gray-700 px-2 py-1 rounded-md border"
                      disabled={item.quantity <= 1}
                    >
                      -
                    </button>
                    <span className="text-gray-600 w-8 text-center">{item.quantity}</span>
                    <button
                      onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)}
                      className="text-gray-500 hover:text-gray-700 px-2 py-1 rounded-md border"
                    >
                      +
                    </button>
                  </div>
                  <button
                    onClick={() => handleRemoveItem(item.id)}
                    className="text-red-500 hover:text-red-700"
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}

            <div className="pt-4 border-t">
              <div className="flex flex-col space-y-2">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal:</span>
                  <span>${totalPrice.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Shipping:</span>
                  <span>Free</span>
                </div>
                <div className="flex justify-between text-xl font-bold text-gray-900 pt-2 border-t">
                  <span>Total:</span>
                  <span>${totalPrice.toFixed(2)}</span>
                </div>
              </div>
              <button
                onClick={handleCheckout}
                className="mt-6 w-full bg-blue-500 text-white px-6 py-3 rounded-lg hover:bg-blue-600 transition-colors duration-300"
              >
                Proceed to Checkout (${totalPrice.toFixed(2)})
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="text-center text-gray-600 py-8">
          Your cart is empty.
        </div>
      )}
    </div>
  );
}

export default Cart; 