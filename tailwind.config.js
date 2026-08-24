/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "#090A0F",
        surface: "#12141C",
        "surface-light": "#1B1E2B",
        "surface-border": "#272B3F",
        primary: {
          50: "#eef2ff",
          100: "#e0e7ff",
          200: "#c7d2fe",
          300: "#a5b4fc",
          400: "#818cf8",
          500: "#6366f1",
          600: "#4f46e5",
          700: "#4338ca",
          800: "#3730a3",
          900: "#312e81",
        },
        accent: {
          cyan: "#00F0FF",
          pink: "#FF007A",
          yellow: "#FFE600",
          green: "#00FF66",
        }
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        anton: ['var(--font-anton)', 'Anton', 'sans-serif'],
        bebas: ['var(--font-bebas)', '"Bebas Neue"', 'sans-serif'],
        archivo: ['var(--font-archivo)', '"Archivo Black"', 'sans-serif'],
        montserrat: ['var(--font-montserrat)', 'Montserrat', 'sans-serif'],
        poppins: ['var(--font-poppins)', 'Poppins', 'sans-serif'],
        baloo: ['var(--font-baloo)', '"Baloo 2"', 'cursive'],
        fredoka: ['var(--font-fredoka)', 'Fredoka', 'sans-serif'],
        cinzel: ['var(--font-cinzel)', 'Cinzel', 'serif'],
        righteous: ['var(--font-righteous)', 'Righteous', 'cursive'],
      },
      keyframes: {
        pop: {
          '0%': { transform: 'scale(0.85)', opacity: '0.8' },
          '50%': { transform: 'scale(1.25)' },
          '100%': { transform: 'scale(1.05)', opacity: '1' }
        },
        bounceCustom: {
          '0%': { transform: 'translateY(15px) scale(0.9)', opacity: '0' },
          '60%': { transform: 'translateY(-6px) scale(1.15)', opacity: '1' },
          '100%': { transform: 'translateY(0) scale(1.05)' }
        },
        slideCustom: {
          '0%': { transform: 'translateX(-25px)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' }
        },
        waveCustom: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-8px)' }
        }
      },
      animation: {
        pop: 'pop 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards',
        bounceCustom: 'bounceCustom 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) forwards',
        slideCustom: 'slideCustom 0.25s ease-out forwards',
        waveCustom: 'waveCustom 0.5s ease-in-out infinite'
      }
    },
  },
  plugins: [],
};
