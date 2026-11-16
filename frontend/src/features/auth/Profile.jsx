import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { User, Mail, Phone, Shield, Save, Check, Package, Clock, MapPin, XCircle, AlertCircle, RefreshCw } from 'lucide-react';
import axiosClient from '../../api/axiosClient';
import orderApi from '../../api/orderApi';
import { useAuth } from '../../context/AuthContext';

export default function Profile() {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  // Tab State: 'profile' or 'orders'
  const activeTab = searchParams.get('tab') === 'orders' ? 'orders' : 'profile';
  const setActiveTab = (tab) => {
    setSearchParams(tab === 'orders' ? { tab: 'orders' } : {});
  };

  const [profile, setProfile] = useState(null);
  const [formData, setFormData] = useState({ firstName: '', lastName: '', phoneNumber: '' });
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  // Orders state
  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [ordersError, setOrdersError] = useState('');
  const [cancellingOrderId, setCancellingOrderId] = useState(null);

  // 1. Load User Profile
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await axiosClient.get('/api/v1/users/me');
        setProfile(response.data);
        setFormData({
          firstName: response.data.firstName || '',
          lastName: response.data.lastName || '',
          phoneNumber: response.data.phoneNumber || '',
        });
      } catch (err) {
        console.error('Failed to load profile:', err);
        // Fallback to auth context
        if (user) {
          setProfile(user);
          setFormData({
            firstName: user.firstName || '',
            lastName: user.lastName || '',
            phoneNumber: user.phoneNumber || '',
          });
        }
      } finally {
        setLoadingProfile(false);
      }
    };

    fetchProfile();
  }, [user]);

  // 2. Load User Orders when orders tab is active
  const fetchOrders = async () => {
    setLoadingOrders(true);
    setOrdersError('');
    try {
      const data = await orderApi.getOrders({ page: 0, size: 20 });
      const orderList = data?.content || data || [];
      setOrders(orderList);
    } catch (err) {
      console.warn('Order service query failed:', err);
      setOrdersError('Unable to fetch orders from Order Service.');
    } finally {
      setLoadingOrders(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'orders') {
      fetchOrders();
    }
  }, [activeTab]);

  // Handle Profile Update
  const handleUpdate = async (e) => {
    e.preventDefault();
    setSavingProfile(true);
    setSuccessMsg('');
    setErrorMsg('');

    try {
      const response = await axiosClient.put('/api/v1/users/me', formData);
      setProfile(response.data);
      setSuccessMsg('Profile updated successfully!');
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      console.error('Update failed:', err);
      setErrorMsg('Failed to update profile.');
    } finally {
      setSavingProfile(false);
    }
  };

  // Handle Order Cancellation
  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;
    setCancellingOrderId(orderId);
    try {
      await orderApi.cancelOrder(orderId, 'Cancelled by user request');
      await fetchOrders();
    } catch (err) {
      alert(`Could not cancel order: ${err.response?.data?.message || err.message}`);
    } finally {
      setCancellingOrderId(null);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'DELIVERED':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      case 'SHIPPED':
        return 'bg-indigo-100 text-indigo-800 border-indigo-200';
      case 'PROCESSING':
        return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'CANCELLED':
        return 'bg-rose-100 text-rose-800 border-rose-200';
      default:
        return 'bg-blue-100 text-blue-800 border-blue-200';
    }
  };

  if (loadingProfile) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      
      {/* Header Banner */}
      <div className="bg-slate-900 rounded-3xl p-8 mb-8 text-white shadow-sm flex flex-col sm:flex-row items-center gap-6 justify-between">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-indigo-600 flex items-center justify-center text-2xl font-bold shadow-md">
            {profile?.firstName?.charAt(0) || user?.email?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <div>
            <h1 className="text-2xl font-bold">
              {profile?.firstName ? `${profile.firstName} ${profile.lastName || ''}` : 'Nexkart Member'}
            </h1>
            <p className="text-slate-400 text-xs sm:text-sm flex items-center gap-1.5 mt-1">
              <Mail className="w-3.5 h-3.5 text-indigo-400" /> {profile?.email || user?.email}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <span className="px-3.5 py-1 bg-white/10 text-indigo-300 text-xs font-semibold rounded-full border border-white/10 uppercase tracking-wider">
            {profile?.role || user?.role || 'CUSTOMER'}
          </span>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="flex border-b border-slate-200 mb-8 gap-8">
        <button
          onClick={() => setActiveTab('profile')}
          className={`pb-3 text-sm font-semibold transition relative ${
            activeTab === 'profile'
              ? 'text-indigo-600 border-b-2 border-indigo-600'
              : 'text-slate-500 hover:text-slate-900'
          }`}
        >
          <span className="flex items-center gap-2">
            <User className="w-4 h-4" /> Personal Information
          </span>
        </button>

        <button
          onClick={() => setActiveTab('orders')}
          className={`pb-3 text-sm font-semibold transition relative ${
            activeTab === 'orders'
              ? 'text-indigo-600 border-b-2 border-indigo-600'
              : 'text-slate-500 hover:text-slate-900'
          }`}
        >
          <span className="flex items-center gap-2">
            <Package className="w-4 h-4" /> Order History
          </span>
        </button>
      </div>

      {/* TAB CONTENT: PROFILE FORM */}
      {activeTab === 'profile' && (
        <div className="bg-white rounded-2xl border border-slate-200 p-8 shadow-xs">
          {successMsg && (
            <div className="mb-6 p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-xl flex items-center gap-2">
              <Check className="w-4 h-4" /> {successMsg}
            </div>
          )}

          {errorMsg && (
            <div className="mb-6 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl">
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleUpdate} className="space-y-6">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                  First Name
                </label>
                <input
                  type="text"
                  required
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-indigo-600 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                  Last Name
                </label>
                <input
                  type="text"
                  required
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-indigo-600 focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                Phone Number
              </label>
              <input
                type="text"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                placeholder="+1 (555) 000-0000"
                className="w-full px-3.5 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-indigo-600 focus:outline-none"
              />
            </div>

            <div className="pt-4 border-t border-slate-100 flex justify-end">
              <button
                type="submit"
                disabled={savingProfile}
                className="flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-xl shadow-xs transition disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                {savingProfile ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* TAB CONTENT: ORDER HISTORY */}
      {activeTab === 'orders' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-slate-900">Your Placed Orders</h2>
            <button
              onClick={fetchOrders}
              disabled={loadingOrders}
              className="flex items-center gap-1.5 text-xs text-indigo-600 hover:text-indigo-800 font-semibold transition"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loadingOrders ? 'animate-spin' : ''}`} />
              Refresh
            </button>
          </div>

          {loadingOrders ? (
            <div className="py-20 text-center">
              <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
              <p className="text-xs text-slate-500">Retrieving your orders from Order Service...</p>
            </div>
          ) : ordersError ? (
            <div className="p-4 bg-amber-50 border border-amber-200 text-amber-800 text-xs rounded-xl flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{ordersError} Make sure order-service is running on port 8084.</span>
            </div>
          ) : orders.length === 0 ? (
            <div className="text-center py-20 bg-white rounded-2xl border border-slate-200 p-8">
              <div className="w-14 h-14 bg-slate-50 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-3">
                <Package className="w-7 h-7" />
              </div>
              <h3 className="text-base font-bold text-slate-800">No orders placed yet</h3>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                Explore our catalog, add items to your cart, and place an order using our microservices pipeline!
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {orders.map((order) => {
                const canCancel = order.status === 'CREATED' || order.status === 'PROCESSING';
                return (
                  <div
                    key={order.id}
                    className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs hover:border-slate-300 transition"
                  >
                    {/* Order Top Summary */}
                    <div className="p-5 bg-slate-50/80 border-b border-slate-200 flex flex-wrap items-center justify-between gap-4">
                      <div>
                        <div className="text-[11px] font-medium text-slate-500">Order Reference</div>
                        <div className="text-sm font-bold text-indigo-600 font-mono">
                          {order.orderNumber || `#ORD-${order.id}`}
                        </div>
                      </div>

                      <div>
                        <div className="text-[11px] font-medium text-slate-500">Order Date</div>
                        <div className="text-xs font-semibold text-slate-700">
                          {order.createdAt ? new Date(order.createdAt).toLocaleDateString() : 'Recent'}
                        </div>
                      </div>

                      <div>
                        <div className="text-[11px] font-medium text-slate-500">Total Amount</div>
                        <div className="text-sm font-extrabold text-slate-900">
                          ${Number(order.totalAmount || 0).toFixed(2)}
                        </div>
                      </div>

                      <div className="flex items-center gap-3 ml-auto">
                        <span className={`px-2.5 py-1 text-[11px] font-bold rounded-md border ${getStatusBadge(order.status)}`}>
                          {order.status}
                        </span>

                        {canCancel && (
                          <button
                            onClick={() => handleCancelOrder(order.id)}
                            disabled={cancellingOrderId === order.id}
                            className="text-xs text-rose-600 hover:text-rose-800 font-semibold transition px-2.5 py-1 rounded-lg hover:bg-rose-50"
                          >
                            {cancellingOrderId === order.id ? 'Cancelling...' : 'Cancel'}
                          </button>
                        )}
                      </div>
                    </div>

                    {/* Order Body Details */}
                    <div className="p-5">
                      <div className="text-xs font-semibold text-slate-500 mb-2 flex items-center gap-1.5">
                        <MapPin className="w-3.5 h-3.5 text-slate-400" />
                        <span>Shipping Address: <strong className="text-slate-800 font-medium">{order.shippingAddress || 'Standard Delivery'}</strong></span>
                      </div>

                      {/* Items */}
                      <div className="divide-y divide-slate-100 mt-3">
                        {(order.items || order.orderLines || []).map((line, idx) => (
                          <div key={idx} className="py-2.5 flex items-center justify-between text-xs">
                            <div>
                              <span className="font-semibold text-slate-800">
                                {line.productName || `Product #${line.productId}`}
                              </span>
                              <span className="text-slate-400 ml-2">SKU: {line.sku || 'N/A'}</span>
                            </div>
                            <div className="font-medium text-slate-700">
                              Qty: {line.quantity} × ${Number(line.unitPrice || 0).toFixed(2)} = <span className="font-bold text-slate-900">${Number(line.totalPrice || (line.unitPrice * line.quantity) || 0).toFixed(2)}</span>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>

                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

    </div>
  );
}
