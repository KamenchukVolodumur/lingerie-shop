import React, { createContext, useCallback, useContext, useState } from 'react';

const CartContext = createContext();

export function CartProvider({ children }) {
  const [cartItems, setCartItems] = useState([]);
  const [isCartOpen, setIsCartOpen] = useState(false);

  const addToCart = (product, quantity = 1) => {
    setCartItems(prevItems => {
      // Check if product with same id, size, and color already exists
      const existingItemIndex = prevItems.findIndex(
        item => item.id === product.id && 
                item.productVariantId === product.productVariantId
      );

      if (existingItemIndex >= 0) {
        // Item exists, update quantity
        const newItems = [...prevItems];
        newItems[existingItemIndex].quantity += quantity;
        return newItems;
      } else {
        // New item
        return [...prevItems, { ...product, quantity }];
      }
    });
    setIsCartOpen(true);
  };

  const removeFromCart = (indexToRemove) => {
    setCartItems(cartItems.filter((_, index) => index !== indexToRemove));
  };

  const updateQuantity = (indexToUpdate, newQuantity) => {
    if (newQuantity < 1) return; // Prevent 0 or negative
    setCartItems(prevItems => {
      const newItems = [...prevItems];
      newItems[indexToUpdate].quantity = newQuantity;
      return newItems;
    });
  };

  const cartTotal = cartItems.reduce(
    (total, item) => total + (Number(item.price) * item.quantity), 0
  ).toFixed(2);

  const cartCount = cartItems.reduce((count, item) => count + item.quantity, 0);

  const clearCart = useCallback(() => {
    setCartItems([]);
  }, []);

  return (
    <CartContext.Provider value={{
      cartItems,
      isCartOpen,
      setIsCartOpen,
      addToCart,
      removeFromCart,
      updateQuantity,
      clearCart,
      cartTotal,
      cartCount
    }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
