import { useState, useEffect } from "react";
import { orderService } from "../services/orderService";

function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const data = await orderService.getOrders();
      console.log("Orders data:", data); // Debug log
      setOrders(data);
      setError("");
    } catch (err) {
      setError("Failed to fetch orders");
      console.error("Error fetching orders:", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-900">Order History</h2>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {orders.length > 0 ? (
        <div className="space-y-4">
          {orders.map((order) => (
            <div
              key={order.id}
              src={console.log(order)}
              className="bg-white rounded-lg shadow-md overflow-hidden"
            >
              <div className="p-6">
                <div className="flex justify-between items-center mb-4">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">
                      Order #{order.id}
                    </h3>
                    <p className="text-sm text-gray-500">
                      Placed on:{" "}
                      {new Date(order.orderDate).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-semibold text-gray-900">
                      Total: ${order.totalAmount}
                    </p>
                    <p className="text-sm text-gray-500">
                      Status: {order.status}
                    </p>
                  </div>
                </div>

                <div className="border-t pt-4">
                  <h4 className="text-md font-semibold text-gray-900 mb-3">
                    Items
                  </h4>
                  <div className="space-y-3">
                    {order.items &&
                      order.items.map((item) => {
                        return (
                          <div
                            key={item.id}
                            className="flex items-center justify-between"
                          >
                            <div className="flex items-center space-x-4">
                              <img
                                src={`http://localhost:8080/api/images/${item.imageUrl}`}
                                alt={item.productName || "Product"}
                                className="w-20 h-20 object-cover rounded"
                                onError={(e) => {
                                  e.target.src = "/placeholder-image.jpg  ";
                                }}
                              />
                              <div>
                                <h5 className="font-semibold text-gray-900">
                                  {item.productName ||
                                    "Product Name Not Available"}
                                </h5>
                                <p className="text-sm text-gray-500">
                                  Quantity: {item.quantity}
                                </p>
                              </div>
                            </div>
                            <p className="text-gray-600">
                              ${(item.price * item.quantity).toFixed(2)}
                            </p>
                          </div>
                        );
                      })}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center text-gray-600 py-8">No orders found.</div>
      )}
    </div>
  );
}

export default Orders;
