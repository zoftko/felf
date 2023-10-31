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
}
