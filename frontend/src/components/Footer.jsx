import React from 'react';

export default function Footer() {
  return (
    <footer className="bg-slate-900 text-slate-400 py-12 mt-auto border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <span className="font-bold text-xl text-white tracking-tight">
              Nex<span className="text-indigo-400">kart</span>
            </span>
            <p className="mt-3 text-sm text-slate-400 leading-relaxed">
              Enterprise event-driven microservices e-commerce platform. Built with Spring Boot 3, PostgreSQL, Redis, and React.
            </p>
          </div>
          <div>
            <h4 className="text-xs font-semibold text-slate-200 uppercase tracking-wider">Microservices</h4>
            <ul className="mt-3 space-y-2 text-sm">
              <li><span className="hover:text-white transition">User & Auth Service (:8081)</span></li>
              <li><span className="hover:text-white transition">Product Catalog (:8082)</span></li>
              <li><span className="hover:text-white transition">Cart Service (:8085)</span></li>
              <li><span className="hover:text-white transition">Order & Inventory (:8084/:8083)</span></li>
            </ul>
          </div>
          <div>
            <h4 className="text-xs font-semibold text-slate-200 uppercase tracking-wider">Gateway & Registry</h4>
            <ul className="mt-3 space-y-2 text-sm">
              <li><span className="hover:text-white transition">API Gateway (:8080)</span></li>
              <li><span className="hover:text-white transition">Eureka Server (:8761)</span></li>
              <li><span className="hover:text-white transition">Neon PostgreSQL Cloud</span></li>
            </ul>
          </div>
          <div>
            <h4 className="text-xs font-semibold text-slate-200 uppercase tracking-wider">Team Architecture</h4>
            <p className="mt-3 text-sm text-slate-400 leading-relaxed">
              Each microservice module owns its database schema and UI feature domain.
            </p>
          </div>
        </div>
        <div className="mt-8 pt-8 border-t border-slate-800 text-center text-xs text-slate-500">
          © {new Date().getFullYear()} Nexkart Team. All rights reserved.
        </div>
      </div>
    </footer>
  );
}
