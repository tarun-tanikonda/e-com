import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Sparkles, Filter, AlertCircle } from 'lucide-react';
import ProductCard from './ProductCard';
import axiosClient from '../../api/axiosClient';

// Fallback products so the UI is immediately beautiful for demos
const MOCK_PRODUCTS = [
  {
    id: 1,
    name: 'Nexkart Noise Cancelling Headphones',
    description: 'Wireless Bluetooth over-ear headphones with active noise cancellation and 40h battery.',
    basePrice: 199.99,
    categoryName: 'Audio',
    imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=60',
  },
  {
    id: 2,
    name: 'Ultra Slim Smartwatch Series X',
    description: 'AMOLED display, heart-rate tracking, sleep analytics, and 5 ATM water resistance.',
    basePrice: 149.50,
    categoryName: 'Wearables',
    imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=60',
  },
  {
    id: 3,
    name: 'Mechanical Ergonomic Gaming Keyboard',
    description: 'Hot-swappable tactile switches with customizable RGB lighting and aircraft-grade aluminum.',
    basePrice: 89.99,
    categoryName: 'Peripherals',
    imageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600&auto=format&fit=crop&q=60',
  },
  {
    id: 4,
    name: 'Minimalist Leather Desk Pad',
    description: 'Water-resistant vegan PU leather mouse pad with non-slip suede base for clean workstations.',
    basePrice: 29.99,
    categoryName: 'Accessories',
    imageUrl: 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600&auto=format&fit=crop&q=60',
  },
];

export default function ProductCatalog({ onAddToCart }) {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchParams] = useSearchParams();

  const searchKeyword = searchParams.get('search') || '';

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      setError('');
      try {
        // Calls API Gateway -> Product Service Catalog
        const response = await axiosClient.get('/api/v1/products/catalog');
        const data = response.data?.content || response.data || [];
        setProducts(data.length > 0 ? data : MOCK_PRODUCTS);
      } catch (err) {
        console.warn('Backend product-service not reachable yet; falling back to demo catalog.');
        setProducts(MOCK_PRODUCTS);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  // Filter products by search and category
  const filteredProducts = products.filter((p) => {
    const matchesSearch = !searchKeyword || 
      p.name?.toLowerCase().includes(searchKeyword.toLowerCase()) ||
      p.description?.toLowerCase().includes(searchKeyword.toLowerCase());
    
    const matchesCategory = selectedCategory === 'ALL' || 
      (p.categoryName || p.category?.name) === selectedCategory;

    return matchesSearch && matchesCategory;
  });

  const categories = ['ALL', ...new Set(products.map((p) => p.categoryName || p.category?.name).filter(Boolean))];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      
      {/* Hero Banner */}
      <div className="relative rounded-3xl bg-gradient-to-r from-indigo-900 via-indigo-800 to-slate-900 p-8 sm:p-12 mb-10 text-white overflow-hidden shadow-sm">
        <div className="relative z-10 max-w-2xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur rounded-full text-xs font-semibold text-indigo-200 mb-4">
            <Sparkles className="w-3.5 h-3.5" /> Nexkart Next-Gen Shopping
          </div>
          <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight leading-tight">
            Curated Products. Exceptional Quality.
          </h1>
          <p className="mt-3 text-slate-300 text-sm sm:text-base leading-relaxed">
            Welcome to Nexkart — Explore our premium selection of technology, audio, wearables, and accessories.
          </p>
        </div>
      </div>

      {/* Category Filter Pills */}
      <div className="flex items-center justify-between mb-8 overflow-x-auto pb-2 gap-4">
        <div className="flex items-center gap-2">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-4 py-2 text-xs font-semibold rounded-full transition whitespace-nowrap ${
                selectedCategory === cat
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'bg-white text-slate-600 hover:bg-slate-100 border border-slate-200'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        <span className="text-xs text-slate-400 font-medium shrink-0">
          Showing {filteredProducts.length} items
        </span>
      </div>

      {/* Product Grid */}
      {loading ? (
        <div className="min-h-[40vh] flex items-center justify-center">
          <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : filteredProducts.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <AlertCircle className="w-8 h-8 text-slate-400 mx-auto mb-2" />
          <h3 className="text-base font-semibold text-slate-800">No products found</h3>
          <p className="text-xs text-slate-500 mt-1">Try clearing your search query or switching categories.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {filteredProducts.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onAddToCart={onAddToCart}
            />
          ))}
        </div>
      )}

    </div>
  );
}
