/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: '#ffffff',
        foreground: '#222222', // Dark grey, softer than black
        primary: '#f9f5f0', // Very light beige / powder
        accent: '#e3d9e8', // Pastel lavender
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'], // Clean modern font
      }
    },
  },
  plugins: [],
}
