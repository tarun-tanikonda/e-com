import React from 'react';
import { ShoppingBag } from 'lucide-react';

export default function ProductCard({ product, onAddToCart }) {
  // Fallback defaults if product structure differs
  const price = product?.basePrice || product?.price || 49.99;
  const name = product?.name || 'Nexkart Product';
  const category = product?.categoryName || product?.category?.name || 'Electronics';
  const description = product?.description || 'High performance quality merchandise from Nexkart.';

  return (
    <div className="group bg-white rounded-2xl border border-slate-200 overflow-hidden hover:shadow-lg transition-all duration-300 flex flex-col">
      
      {/* Product Image Box */}
      <div className="relative aspect-square bg-slate-100 overflow-hidden flex items-center justify-center">
        <img
          src={product?.imageUrl || `https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=60`}
          alt={name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          onError={(e) => {
            e.target.onerror = null;
            e.target.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect width='400' height='400' fill='%23f1f5f9'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' font-family='sans-serif' font-size='16' fill='%2394a3b8'%3ENexkart Product%3C/text%3E%3C/svg%3E";
          }}
        />
        <div className="absolute top-3 left-3 bg-white/90 backdrop-blur px-2.5 py-1 rounded-full text-[11px] font-semibold text-slate-700 uppercase tracking-wider">
          {category}
        </div>
      </div>

      {/* Product Body */}
      <div className="p-5 flex-1 flex flex-col">
        <h3 className="font-semibold text-slate-900 group-hover:text-indigo-600 transition line-clamp-1">
          {name}
        </h3>
        <p className="text-xs text-slate-500 mt-1 line-clamp-2 leading-relaxed">
          {description}
        </p>

        {/* Price & Action */}
        <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
          <div>
            <span className="text-xs text-slate-400 block font-medium">Price</span>
            <span className="text-lg font-bold text-slate-900">${Number(price).toFixed(2)}</span>
          </div>

          <button
            onClick={() => onAddToCart(product)}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-50 hover:bg-indigo-600 text-indigo-600 hover:text-white text-xs font-semibold rounded-xl transition duration-200 active:scale-95"
          >
            <ShoppingBag className="w-3.5 h-3.5" />
            Add
          </button>
        </div>
      </div>

    </div>
  );
}
