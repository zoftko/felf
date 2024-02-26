/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/main/resources/templates/**/*.html"],
  theme: {
    extend: {},
  },
  corePlugins: {
    preflight: true,
  },
  plugins: [
      require("@tailwindcss/typography"),
      require("daisyui")
  ],
  daisyui: {
    themes: [
      {
        light: {
          ...require("daisyui/src/theming/themes")["[data-theme=light]"],
          ".bg-surface-100": {
            "background-color": "hsl(0 0% 98%)"
          },
          ".border-surface-100": {
            "border-color": "hsl(0 0% 90%)"
          },
        }
      },
      {
        dark: {
          ...require("daisyui/src/theming/themes")["[data-theme=dark]"],
          ".bg-surface-100": {
            "background-color": "hsl(212 18% 16%)"
          },
          ".border-surface-100": {
            "border-color": "hsl(212 18% 18%)"
          },
        }
      },
    ]
  }
}
