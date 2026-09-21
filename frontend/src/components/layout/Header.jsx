import React, { useState } from 'react';
import { Search, ShoppingBag, User, Menu, LogOut } from 'lucide-react';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';
import SearchOverlay from './SearchOverlay';

function Header({ setIsMenuOpen }) {
  const { cartCount, setIsCartOpen } = useCart();
  const { isAuthenticated, logout } = useAuth();
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-20">
          
          {/* Catalog / Menu button */}
          <div className="flex items-center flex-1">
            <button 
              onClick={() => setIsMenuOpen(true)} 
              className="flex items-center p-2 -ml-2 text-gray-900 hover:text-gray-500 transition-colors"
            >
              <Menu size={24} strokeWidth={1.5} className="mr-3" />
              <span className="hidden lg:block text-sm font-medium tracking-wide uppercase">Каталог</span>
            </button>
          </div>

          {/* Logo */}
          <div className="flex-shrink-0 flex items-center justify-center absolute left-1/2 -translate-x-1/2">
            <Link to="/" className="text-2xl tracking-[0.2em] font-light uppercase">
              DRESS CODE
            </Link>
          </div>

          {/* Right Icons */}
          <div className="flex items-center justify-end flex-1 space-x-6">
            <button 
              className="text-gray-900 hover:text-gray-500 transition-colors"
              onClick={() => setIsSearchOpen(true)}
            >
              <Search size={22} strokeWidth={1.2} />
            </button>
            
            {isAuthenticated ? (
              <>
                <Link to="/account" className="text-gray-900 hover:text-gray-500 transition-colors hidden sm:block" title="Особистий кабінет">
                  <User size={22} strokeWidth={1.2} />
                </Link>
                <button
                  onClick={logout}
                  className="text-gray-900 hover:text-gray-500 transition-colors hidden sm:flex items-center"
                  title="Вийти"
                >
                  <LogOut size={22} strokeWidth={1.2} />
                </button>
              </>
            ) : (
              <Link to="/login" className="text-gray-900 hover:text-gray-500 transition-colors hidden sm:block" title="Увійти">
                <User size={22} strokeWidth={1.2} />
              </Link>
            )}

            <button 
              className="text-gray-900 hover:text-gray-500 transition-colors relative"
              onClick={() => setIsCartOpen(true)}
            >
              <ShoppingBag size={22} strokeWidth={1.2} />
              {cartCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-gray-900 text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center font-medium">
                  {cartCount}
                </span>
              )}
            </button>
          </div>
        </div>
      </div>
      
      <SearchOverlay isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} />
    </header>
  );
}

export default Header;
