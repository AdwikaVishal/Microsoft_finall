# Tailwind CSS PostCSS Fix - TODO List

## Current Issue
The Tailwind CSS PostCSS plugin has been moved to a separate package. Need to update the configuration.

## Fix Steps

### 1. Install @tailwindcss/postcss package
- [x] Install the new package in admin-dashboard directory

### 2. Update postcss.config.js
- [x] Change `tailwindcss` to `@tailwindcss/postcss` in plugins

### 3. Update input.css
- [x] Change `@tailwind` directives to `@import "tailwindcss";`

### 4. Test the fix
- [ ] Run dev server to verify the fix works

