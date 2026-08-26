import React from 'react';
import ProductGrid from '../components/product/ProductGrid';

function Home() {
  return (
    <main className="flex-grow">
      {/* HERO SECTION */}
      <div className="h-[70vh] w-full bg-primary/20 flex items-center justify-center relative overflow-hidden">
        <img 
          src="https://images.unsplash.com/photo-1434389678369-184bf38b14e3?auto=format&fit=crop&q=80&w=2000" 
          alt="Hero" 
          className="absolute inset-0 w-full h-full object-cover object-center opacity-90"
        />
        <div className="absolute inset-0 bg-white/10"></div>
        <div className="relative z-10 text-center">
          <h1 className="text-4xl md:text-6xl font-light tracking-widest text-white mb-6 uppercase drop-shadow-sm">
            Шовкова колекція
          </h1>
          <button className="bg-white text-gray-900 px-8 py-3 text-sm tracking-[0.2em] uppercase hover:bg-gray-100 transition-colors shadow-sm">
            Купити зараз
          </button>
        </div>
      </div>

      <ProductGrid />
    </main>
  );
}

export default Home;
