// src/utils/translations.js

export const attributeKeysUa = {
  material: 'Матеріал',
  fit: 'Посадка',
  color: 'Колір',
  style: 'Стиль',
  pattern: 'Узор',
  size: 'Розмір',
  brand: 'Бренд',
  collection: 'Колекція',
  season: 'Сезон',
  type: 'Тип',
  cup: 'Чашка',
  strap: 'Бретелі'
};

export const attributeValuesUa = {
  // Матеріали
  cotton: 'Бавовна',
  lace: 'Мереживо',
  silk: 'Шовк',
  mesh: 'Сітка',
  microfiber: 'Мікрофібра',
  elastane: 'Еластан',
  satin: 'Атлас',
  
  // Посадка
  slim: 'Облягаюча',
  regular: 'Стандартна',
  loose: 'Вільна',
  high_waist: 'Висока талія',
  low_waist: 'Низька талія',
  
  // Типи (Труси)
  brazilian: 'Бразиліана',
  thong: 'Стрінги',
  slip: 'Сліпи',
  shorts: 'Шортики',
  tanga: 'Танга',
  
  // Типи (Бюстгальтери)
  push_up: 'Пуш-ап',
  balconette: 'Балконет',
  bralette: 'Бралет',
  sports: 'Спортивний',
  
  // Кольори
  black: 'Чорний',
  white: 'Білий',
  red: 'Червоний',
  beige: 'Бежевий',
  pink: 'Рожевий',
  blue: 'Синій',
  navy: 'Темно-синій',
  green: 'Зелений',
  
  // Інше
  solid: 'Однотонний',
  floral: 'Квітковий',
  striped: 'У смужку',
  summer: 'Літо',
  winter: 'Зима',
  all_season: 'Всесезонний'
};

export const translateKey = (key) => {
  if (!key) return '';
  return attributeKeysUa[key.toLowerCase()] || key;
};

export const translateValue = (value) => {
  if (!value) return '';
  return attributeValuesUa[value.toLowerCase()] || value;
};
