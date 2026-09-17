import React, { useState, useEffect } from 'react';
import { categoryService } from '../../api/categoryService';
import { translateKey, translateValue } from '../../utils/translations';

function CategorySidebar({ categoryId, onFiltersChange }) {
  const [filters, setFilters] = useState({});
  const [selectedFilters, setSelectedFilters] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchFilters = async () => {
      setIsLoading(true);
      try {
        const data = await categoryService.getCategoryFilters(categoryId);
        setFilters(data || {});
        // Reset selected filters when category changes
        setSelectedFilters({});
        onFiltersChange({});
      } catch (error) {
        console.error("Error fetching filters", error);
      } finally {
        setIsLoading(false);
      }
    };
    
    if (categoryId) {
      fetchFilters();
    }
  }, [categoryId]);

  const handleFilterChange = (key, value) => {
    setSelectedFilters(prev => {
      const currentValues = prev[key] || [];
      let newValues;
      
      if (currentValues.includes(value)) {
        newValues = currentValues.filter(v => v !== value);
      } else {
        newValues = [...currentValues, value];
      }
      
      const newFilters = { ...prev, [key]: newValues };
      // Clean up empty filters
      if (newValues.length === 0) {
        delete newFilters[key];
      }
      
      onFiltersChange(newFilters);
      return newFilters;
    });
  };

  if (isLoading) {
    return <div className="text-sm text-gray-500 p-4">Завантаження фільтрів...</div>;
  }

  if (Object.keys(filters).length === 0) {
    return null; // No filters available
  }

  return (
    <div className="w-full md:w-64 flex-shrink-0 pr-8 mb-8 md:mb-0">
      <h3 className="text-sm font-medium uppercase tracking-widest text-gray-900 mb-6">Фільтри</h3>
      
      <div className="space-y-8">
        {Object.entries(filters).map(([key, values]) => (
          <div key={key}>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-700 mb-3">{translateKey(key)}</h4>
            <div className="space-y-2">
              {values.map(value => (
                <label key={value} className="flex items-center space-x-3 cursor-pointer group">
                  <input
                    type="checkbox"
                    className="form-checkbox h-4 w-4 text-gray-900 border-gray-300 rounded-sm focus:ring-gray-900 transition-colors cursor-pointer"
                    checked={(selectedFilters[key] || []).includes(value)}
                    onChange={() => handleFilterChange(key, value)}
                  />
                  <span className="text-sm text-gray-600 group-hover:text-gray-900 transition-colors">{translateValue(value)}</span>
                </label>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default CategorySidebar;
