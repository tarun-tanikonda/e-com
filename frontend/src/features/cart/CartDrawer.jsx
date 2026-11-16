import React, { useState } from 'react';
import { X, Trash2, Plus, Minus, ArrowRight, ShoppingBag, ArrowLeft, CheckCircle2, AlertCircle, MapPin, Loader2, LogIn, UserCheck } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import orderApi from '../../api/orderApi';

export default function CartDrawer({
  isOpen,
  onClose,
  items = [],
  onUpdateQuantity,
  onRemoveItem,
  onClearCart,
}) {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [shippingAddress, setShippingAddress] = useState('123 Innovation Way, Suite 400, Tech District');
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [orderSuccess, setOrderSuccess] = useState(null);

  if (!isOpen) return null;

  const subtotal = items.reduce(
    (sum, item) => sum + (item.price || item.basePrice || 0) * (item.quantity || 1),
    0
  );

  const handleStartCheckout = () => {
    if (!isAuthenticated) {
      onClose();
      navigate('/login');
      return;
    }
    setIsCheckingOut(true);
    setErrorMsg('');
  };

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    if (!shippingAddress.trim()) {
      setErrorMsg('Please enter a valid shipping address.');
      return;
    }

    setSubmitting(true);
    setErrorMsg('');

    try {
      const orderPayload = {
        shippingAddress: shippingAddress.trim(),
        currency: 'USD',
        discount: 0,
        shipping: 0,
        tax: 0,
        items: items.map((item) => ({
          productId: item.id || 1,
          sku: item.sku || `SKU-${item.id || 1}`,
          quantity: item.quantity || 1,
          unitPrice: Number(item.price || item.basePrice || 49.99),
          productName: item.name || 'Nexkart Item',
        })),
      };

      const placedOrder = await orderApi.createOrder(orderPayload);
      setOrderSuccess(placedOrder);
      onClearCart();
    } catch (err) {
      console.error('Order placement failed:', err);
      if (err.response?.status === 401) {
        setErrorMsg('Your session has expired. Please sign in again to place your order.');
        logout();
      } else {
        const serverMsg = err.response?.data?.message || err.response?.data?.error || err.message;
        setErrorMsg(`Failed to place order: ${serverMsg}`);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleResetAndClose = () => {
    setIsCheckingOut(false);
    setOrderSuccess(null);
    setErrorMsg('');
    onClose();
  };

  const handleViewOrders = () => {
    handleResetAndClose();
    navigate('/profile?tab=orders');
  };

  const handleGoToLogin = () => {
    handleResetAndClose();
    navigate('/login');
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Backdrop */}
      <div
        onClick={handleResetAndClose}
        className="absolute inset-0 bg-slate-900/40 backdrop-blur-xs transition-opacity"
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col">
          
          {/* Header */}
          <div className="px-6 py-5 border-b border-slate-200 flex items-center justify-between">
            <div className="flex items-center gap-2">
              {isCheckingOut && !orderSuccess && (
                <button
                  onClick={() => setIsCheckingOut(false)}
                  className="p-1 text-slate-400 hover:text-slate-700 rounded mr-1 transition"
                  title="Back to Cart"
                >
                  <ArrowLeft className="w-5 h-5" />
                </button>
              )}
              <ShoppingBag className="w-5 h-5 text-indigo-600" />
              <h2 className="text-lg font-bold text-slate-900">
                {orderSuccess ? 'Order Confirmed' : isCheckingOut ? 'Express Checkout' : 'Your Cart'}
              </h2>
              {!isCheckingOut && !orderSuccess && (
                <span className="text-xs bg-indigo-50 text-indigo-700 font-semibold px-2 py-0.5 rounded-full">
                  {items.length}
                </span>
              )}
            </div>
            <button
              onClick={handleResetAndClose}
              className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-100 transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Drawer Body */}
          <div className="flex-1 overflow-y-auto px-6 py-4">
            
            {/* SUCCESS VIEW */}
            {orderSuccess ? (
              <div className="py-12 text-center flex flex-col items-center">
                <div className="w-16 h-16 bg-emerald-50 text-emerald-600 rounded-full flex items-center justify-center mb-4">
                  <CheckCircle2 className="w-10 h-10" />
                </div>
                <h3 className="text-xl font-bold text-slate-900">Thank you for your order!</h3>
                <p className="text-xs text-slate-500 mt-2 max-w-xs">
                  Your order has been recorded in the Nexkart Order Service.
                </p>
                <div className="my-6 p-4 bg-slate-50 border border-slate-200 rounded-xl w-full text-left">
                  <div className="text-xs text-slate-500 font-medium">Order Number</div>
                  <div className="text-sm font-bold text-indigo-600 font-mono mt-0.5">
                    {orderSuccess.orderNumber || `#ORD-${orderSuccess.id || 'NEX'}`}
                  </div>
                  <div className="mt-3 text-xs text-slate-500 font-medium">Delivery Destination</div>
                  <div className="text-xs text-slate-800 font-medium mt-0.5">
                    {orderSuccess.shippingAddress || shippingAddress}
                  </div>
                </div>
                <button
                  onClick={handleViewOrders}
                  className="w-full py-2.5 px-4 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-xl shadow-xs transition"
                >
                  View Order History
                </button>
              </div>
            ) : isCheckingOut ? (
              
              /* CHECKOUT FORM VIEW */
              !isAuthenticated ? (
                <div className="py-16 text-center">
                  <div className="w-14 h-14 bg-indigo-50 text-indigo-600 rounded-full flex items-center justify-center mx-auto mb-4">
                    <LogIn className="w-7 h-7" />
                  </div>
                  <h3 className="text-base font-bold text-slate-900">Sign in to complete checkout</h3>
                  <p className="text-xs text-slate-500 mt-2 max-w-xs mx-auto">
                    Please log in to your Nexkart account so we can securely attach and confirm your order.
                  </p>
                  <button
                    onClick={handleGoToLogin}
                    className="mt-6 w-full py-3 px-4 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl shadow-sm transition"
                  >
                    Go to Login
                  </button>
                </div>
              ) : (
                <form onSubmit={handlePlaceOrder} className="space-y-5">
                  
                  {/* Authenticated user badge */}
                  <div className="p-3 bg-indigo-50/70 border border-indigo-100 rounded-xl flex items-center gap-2.5 text-xs text-indigo-900">
                    <UserCheck className="w-4 h-4 text-indigo-600 shrink-0" />
                    <span>Ordering as <strong className="font-semibold">{user?.email || 'Logged In Customer'}</strong></span>
                  </div>

                  {errorMsg && (
                    <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl flex flex-col gap-2">
                      <div className="flex items-center gap-2">
                        <AlertCircle className="w-4 h-4 shrink-0" />
                        <span>{errorMsg}</span>
                      </div>
                      {errorMsg.includes('expired') && (
                        <button
                          type="button"
                          onClick={handleGoToLogin}
                          className="self-start text-xs font-bold text-indigo-600 hover:underline"
                        >
                          Click here to Log In again →
                        </button>
                      )}
                    </div>
                  )}

                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-indigo-600" /> Shipping Address
                    </label>
                    <textarea
                      rows={3}
                      required
                      value={shippingAddress}
                      onChange={(e) => setShippingAddress(e.target.value)}
                      placeholder="Enter full delivery street address, city, and zip..."
                      className="w-full text-xs text-slate-900 p-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-indigo-600 focus:outline-none"
                    />
                  </div>

                  <div className="p-4 bg-slate-50 rounded-xl border border-slate-200">
                    <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Order Summary</h4>
                    <div className="divide-y divide-slate-200 text-xs">
                      {items.map((item) => (
                        <div key={item.id} className="py-2 flex justify-between">
                          <span className="text-slate-600 truncate max-w-[200px]">
                            {item.name} × {item.quantity}
                          </span>
                          <span className="font-semibold text-slate-900">
                            ${Number((item.price || item.basePrice || 0) * item.quantity).toFixed(2)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={submitting}
                    className="w-full flex items-center justify-center gap-2 py-3 px-4 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-xl shadow-sm transition disabled:opacity-50"
                  >
                    {submitting ? (
                      <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        <span>Placing Order via Gateway...</span>
                      </>
                    ) : (
                      <>
                        <span>Confirm & Place Order (${Number(subtotal).toFixed(2)})</span>
                        <ArrowRight className="w-4 h-4" />
                      </>
                    )}
                  </button>
                </form>
              )
            ) : (

              /* CART ITEM LIST VIEW */
              <div className="divide-y divide-slate-100">
                {items.length === 0 ? (
                  <div className="text-center py-24">
                    <div className="w-16 h-16 bg-slate-50 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-3">
                      <ShoppingBag className="w-8 h-8" />
                    </div>
                    <h3 className="text-base font-semibold text-slate-800">Your cart is empty</h3>
                    <p className="text-xs text-slate-500 mt-1 max-w-xs mx-auto">
                      Looks like you haven't added anything to your cart yet.
                    </p>
                  </div>
                ) : (
                  items.map((item) => {
                    const itemPrice = item.price || item.basePrice || 0;
                    return (
                      <div key={item.id} className="py-4 flex gap-4">
                        <img
                          src={item.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=200&auto=format&fit=crop&q=60'}
                          alt={item.name}
                          className="w-16 h-16 object-cover rounded-xl bg-slate-100 border border-slate-200"
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'%3E%3Crect width='100' height='100' fill='%23f1f5f9'/%3E%3C/svg%3E";
                          }}
                        />
                        <div className="flex-1 flex flex-col justify-between">
                          <div className="flex justify-between items-start">
                            <h4 className="text-sm font-semibold text-slate-900 line-clamp-1">{item.name}</h4>
                            <span className="text-sm font-bold text-slate-900 ml-2">
                              ${Number(itemPrice * item.quantity).toFixed(2)}
                            </span>
                          </div>

                          <div className="flex items-center justify-between mt-2">
                            <div className="flex items-center border border-slate-200 rounded-lg overflow-hidden">
                              <button
                                onClick={() => onUpdateQuantity(item.id, item.quantity - 1)}
                                className="p-1 hover:bg-slate-100 text-slate-600"
                              >
                                <Minus className="w-3.5 h-3.5" />
                              </button>
                              <span className="px-2.5 text-xs font-semibold text-slate-800">{item.quantity}</span>
                              <button
                                onClick={() => onUpdateQuantity(item.id, item.quantity + 1)}
                                className="p-1 hover:bg-slate-100 text-slate-600"
                              >
                                <Plus className="w-3.5 h-3.5" />
                              </button>
                            </div>

                            <button
                              onClick={() => onRemoveItem(item.id)}
                              className="text-slate-400 hover:text-rose-600 transition p-1"
                              title="Remove item"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            )}

          </div>

          {/* Footer Checkout (Only when browsing cart with items) */}
          {!isCheckingOut && !orderSuccess && items.length > 0 && (
            <div className="p-6 border-t border-slate-200 bg-slate-50">
              <div className="flex justify-between items-center mb-4">
                <span className="text-sm text-slate-500 font-medium">Subtotal</span>
                <span className="text-xl font-bold text-slate-900">${Number(subtotal).toFixed(2)}</span>
              </div>
              <p className="text-xs text-slate-400 mb-4">
                Shipping and taxes calculated at checkout.
              </p>

              <button
                onClick={handleStartCheckout}
                className="w-full flex items-center justify-center gap-2 py-3 px-4 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-xl shadow-sm transition active:scale-98"
              >
                <span>Proceed to Checkout</span>
                <ArrowRight className="w-4 h-4" />
              </button>

              <button
                onClick={onClearCart}
                className="w-full mt-2 text-center text-xs text-slate-400 hover:text-slate-600 py-1"
              >
                Clear all items
              </button>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}
