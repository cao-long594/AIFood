---
name: android-java-ui
description: Generate production-ready Android UI code using Java and XML with Android Views, RecyclerView, Material/Card components, and ConstraintLayout/LinearLayout. Use when building or editing Activity, Fragment, Adapter, Dialog, or custom View UI layers in Java-based Android apps, especially list screens, card-based dashboards, and health or nutrition interfaces.
---

# Android Java UI Skill

Follow these rules when generating Android UI code.

## Required Stack

- Use Java for logic code (never Kotlin unless explicitly requested).
- Use XML for UI layout files.
- Use Android Views, RecyclerView, CardView or MaterialCardView, and ConstraintLayout or LinearLayout.

## Architecture and Quality

- Keep code clean, maintainable, and production-ready.
- Prefer readable structure and meaningful naming.
- Reuse components when practical.
- Avoid deprecated APIs unless the user explicitly asks for them.

## UI Output Order

When the request is UI-related, output in this order:

1. XML layout first.
2. Corresponding Java code second.

## UI Composition Rules

- Prefer ConstraintLayout for flexible screens.
- Use RecyclerView for list-based content.
- Use CardView or MaterialCardView for grouped sections.
- Keep spacing, margins, hierarchy, and scanability clear on mobile.
- Use proper IDs and clear variable names.
- Highlight important values clearly in dashboard-style UIs.

## Component Coverage

Be ready to generate complete code for:

- Activity
- Fragment
- RecyclerView Adapter and ViewHolder
- Dialog
- Custom View

## Health and Nutrition UI Guidelines

If the screen is health- or nutrition-related:

- Use card-based sections.
- Emphasize calories, grams, percentages, progress, and key metrics.
- Keep information hierarchy obvious and easy to scan quickly.

## Response Style

- Keep XML and Java code blocks clean and directly runnable after minor project-specific wiring.
- Include only necessary comments.
- Use Material components where appropriate.
