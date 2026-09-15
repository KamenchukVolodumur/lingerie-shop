// Цей маппер компенсує те, що бекенд зараз не віддає фотографії та hex-коди кольорів

// Колекція естетичних фотографій для білизни (mock images)
const MOCK_IMAGES = [
  "https://images.unsplash.com/photo-1588147822002-3837b77ee84c?auto=format&fit=crop&q=80&w=800",
  "https://images.unsplash.com/photo-1616428416868-b3d4f82beae7?auto=format&fit=crop&q=80&w=800",
  "https://images.unsplash.com/photo-1596765792949-14a0fae9f7cc?auto=format&fit=crop&q=80&w=800",
  "https://images.unsplash.com/photo-1593030364998-cb542718e27c?auto=format&fit=crop&q=80&w=800",
  "https://images.unsplash.com/photo-1620862025381-8b2b73bc7010?auto=format&fit=crop&q=80&w=800",
  "https://images.unsplash.com/photo-1611738722421-26c71c4d96c9?auto=format&fit=crop&q=80&w=800",
];

// Словник кольорів: Назва (з бекенду) -> Hex код
const COLOR_MAP = {
  "Чорний": "#222222",
  "Білий": "#ffffff",
  "Червоний": "#b91c1c",
  "Бежевий": "#D4B895",
  "Пудровий": "#f4ece6",
  "Шампань": "#F7E7CE",
  "Синій": "#1e3a8a",
  "Смарагдовий": "#3b5e4d",
};

export const mapProductFromBackend = (productDto) => {
  // Генеруємо "псевдо-випадкові" (але стабільні для одного ID) індекси для картинок
  const idHash = Number(productDto.id) || 0;
  const image1 = productDto.imageUrl || MOCK_IMAGES[idHash % MOCK_IMAGES.length];
  const image2 = productDto.imageUrl || MOCK_IMAGES[(idHash + 1) % MOCK_IMAGES.length];

  // Групуємо варіанти з бекенду (де колір і розмір плоскі) в унікальні кольори
  const uniqueColorsMap = new Map();
  let totalStock = 0;

  if (productDto.variants) {
    productDto.variants.forEach(variant => {
      totalStock += (variant.stock || 0);
      
      if (!uniqueColorsMap.has(variant.color)) {
        uniqueColorsMap.set(variant.color, {
          name: variant.color || "Стандартний",
          hex: COLOR_MAP[variant.color] || "#cccccc", // Default gray if not found
          image: image1,
          hoverImage: image2,
          // Store sizes available for this specific color
          availableSizes: []
        });
      }
      // Add size if in stock
      if (variant.stock > 0) {
        uniqueColorsMap.get(variant.color).availableSizes.push({
          variantId: variant.id,
          size: variant.size,
          stock: variant.stock
        });
      }
    });
  }

  const colors = Array.from(uniqueColorsMap.values());
  
  // Якщо варіантів немає взагалі (порожній масив), додаємо дефолтний
  if (colors.length === 0) {
    colors.push({
      name: "Стандартний",
      hex: "#222222",
      image: image1,
      hoverImage: image2,
      availableSizes: [{ size: "M", stock: 10 }]
    });
  }

  return {
    id: productDto.id,
    name: productDto.name,
    slug: productDto.slug,
    description: productDto.description,
    imageUrl: productDto.imageUrl,
    price: `$${parseFloat(productDto.price).toFixed(2)}`,
    categoryId: productDto.category,
    inStock: totalStock > 0,
    colors: colors,
    // Original raw variants for strict checking later if needed
    rawVariants: productDto.variants
  };
};
