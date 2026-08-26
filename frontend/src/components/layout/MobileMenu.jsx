import React from 'react';
import { X } from 'lucide-react';

function MobileMenu({ isMenuOpen, setIsMenuOpen }) {
  if (!isMenuOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex lg:hidden">
      <div 
        className="fixed inset-0 bg-black/20 backdrop-blur-sm transition-opacity" 
        onClick={() => setIsMenuOpen(false)} 
      />
      <div className="relative flex w-full max-w-xs flex-col overflow-y-auto bg-white pb-12 shadow-xl animate-in slide-in-from-left duration-300">
        <div className="flex px-4 pb-2 pt-5">
          <button 
            onClick={() => setIsMenuOpen(false)} 
            className="-m-2 inline-flex items-center justify-center rounded-md p-2 text-gray-400 hover:text-gray-900 transition-colors"
          >
            <X size={24} strokeWidth={1.5} />
          </button>
        </div>
        <div className="space-y-6 px-4 py-6">
          <div className="flow-root"><a href="#" className="-m-2 block p-2 font-medium text-gray-900">New In</a></div>
          <div className="flow-root"><a href="#" className="-m-2 block p-2 font-medium text-gray-900">Bras</a></div>
          <div className="flow-root"><a href="#" className="-m-2 block p-2 font-medium text-gray-900">Panties</a></div>
          <div className="flow-root"><a href="#" className="-m-2 block p-2 font-medium text-gray-900">Loungewear</a></div>
        </div>
      </div>
    </div>
  );
}

export default MobileMenu;
