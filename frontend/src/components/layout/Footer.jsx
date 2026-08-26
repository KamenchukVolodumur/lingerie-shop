import React from 'react';

function Footer() {
  return (
    <footer className="border-t border-gray-100 bg-white py-16">
      <div className="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12">
          <div>
            <h3 className="text-sm uppercase tracking-widest font-medium mb-6">Обслуговування клієнтів</h3>
            <ul className="space-y-4 text-sm text-gray-500">
              <li><a href="#" className="hover:text-gray-900 transition-colors">Зв'язатися з нами</a></li>
              <li><a href="#" className="hover:text-gray-900 transition-colors">Доставка і повернення</a></li>
              <li><a href="#" className="hover:text-gray-900 transition-colors">Часті питання</a></li>
            </ul>
          </div>
          <div>
            <h3 className="text-sm uppercase tracking-widest font-medium mb-6">Контакти</h3>
            <ul className="space-y-4 text-sm text-gray-500">
              <li><a href="https://instagram.com" target="_blank" rel="noopener noreferrer" className="hover:text-gray-900 transition-colors">Instagram: @aura.lingerie</a></li>
              <li><a href="tel:+380000000000" className="hover:text-gray-900 transition-colors">+38 (000) 000 00 00</a></li>
              <li className="leading-relaxed">вул. Центральна, 1,<br/>м. Київ, Україна</li>
            </ul>
          </div>
          <div className="md:col-span-2">
            <h3 className="text-sm uppercase tracking-widest font-medium mb-6">Розсилка</h3>
            <p className="text-sm text-gray-500 mb-4">Підпишіться, щоб отримувати оновлення, ексклюзивні пропозиції та інше.</p>
            <div className="flex border-b border-gray-300 py-2">
              <input 
                type="email" 
                placeholder="Введіть вашу email адресу" 
                className="appearance-none bg-transparent border-none w-full text-gray-700 mr-3 py-1 px-2 leading-tight focus:outline-none" 
              />
              <button className="flex-shrink-0 text-sm tracking-widest uppercase hover:text-gray-500 transition-colors" type="button">
                Підписатися
              </button>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
