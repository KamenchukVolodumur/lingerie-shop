import React, { useState, useEffect, useRef } from 'react';
import api from '../../api/axiosConfig';

export default function NovaPoshtaAutocomplete({ label, type, cityRef, value, onChange, placeholder }) {
  const [query, setQuery] = useState('');
  const [options, setOptions] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const wrapperRef = useRef(null);

  // Обработка кліку поза компонентом для закриття дропдауну
  useEffect(() => {
    function handleClickOutside(event) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [wrapperRef]);

  // Якщо очистили вибране значення ззовні (або при завантаженні)
  useEffect(() => {
    if (!value) {
      setQuery('');
    }
  }, [value]);

  // Debounce пошук
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      if (isOpen) {
        fetchOptions();
      }
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [query, isOpen, type, cityRef]);

  const fetchOptions = async () => {
    if (type === 'branch' && !cityRef) {
      setOptions([]);
      return;
    }

    setIsLoading(true);
    try {
      let endpoint = '';
      if (type === 'city') {
        endpoint = `/delivery/cities?query=${encodeURIComponent(query)}`;
      } else {
        endpoint = `/delivery/branches?cityRef=${cityRef}&query=${encodeURIComponent(query)}`;
      }
      
      const response = await api.get(endpoint);
      setOptions(response.data || []);
    } catch (error) {
      console.error("Error fetching Nova Poshta data", error);
      setOptions([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelect = (option) => {
    setQuery(option.description);
    onChange(option.ref, option.description);
    setIsOpen(false);
  };

  const handleInputChange = (e) => {
    setQuery(e.target.value);
    setIsOpen(true);
    if (value) {
      // Якщо юзер почав редагувати вже вибране значення, скидаємо вибраний Ref
      onChange('', '');
    }
  };

  return (
    <div ref={wrapperRef} className="relative w-full">
      <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">{label}</label>
      <input
        type="text"
        value={query}
        onChange={handleInputChange}
        onFocus={() => setIsOpen(true)}
        className="w-full border-b border-gray-300 py-3 text-gray-900 focus:outline-none focus:border-gray-900 transition-colors bg-transparent placeholder-gray-400"
        placeholder={placeholder}
        autoComplete="off"
      />
      {isLoading && <span className="absolute right-0 top-9 text-xs text-gray-400">Пошук...</span>}
      
      {isOpen && options.length > 0 && (
        <ul className="absolute z-10 w-full bg-white border border-gray-200 shadow-lg max-h-60 overflow-y-auto mt-1">
          {options.map((option) => (
            <li
              key={option.ref}
              onClick={() => handleSelect(option)}
              className="px-4 py-3 hover:bg-gray-50 cursor-pointer text-sm text-gray-700 border-b border-gray-100 last:border-b-0"
            >
              {option.description}
            </li>
          ))}
        </ul>
      )}
      
      {isOpen && !isLoading && query && options.length === 0 && (
        <div className="absolute z-10 w-full bg-white border border-gray-200 shadow-lg mt-1 px-4 py-3 text-sm text-gray-500">
          Нічого не знайдено
        </div>
      )}
    </div>
  );
}
