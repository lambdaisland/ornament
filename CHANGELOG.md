# Unreleased

## Fixed

- Support using `defstyled` components as reagent form-2 components

# 0.9.87 (2023-04-15 / dac82f4)

## Added

- Added a `:tw-version` flag for the preflight, similar to `set-tokens!`
- Document how to opt-in to Tailwind v3 

# 0.8.84 (2023-02-28 / 8d54daa)

## Added

- Implement inheritance for fn-tails

# 0.7.77 (2022-11-25 / a1f8d65)

## Added

- Add Clerk garden setup

## Fixed

- improved way to handle girouette v2 and v3 tokens

# 0.6.69 (2022-10-11 / a629407)

## Fixed

- Fixed an issue withe direct invocation of components with a render function (tail-fn)

# 0.5.65 (2022-09-20 / 94cbebe)

## Added

- Support attributes when using a top-level fragment in a rendering function

# 0.4.34 (2022-01-25 / df056c8)

## Fixed

- Fix cljdoc build

# 0.3.30 (2022-01-25 / d37c5e4)

## Fixed

- Improve ClojureScript support, in particular referencing components in other components style rules
- Support vectors with multiple selectors, plus alternative syntax with sets

# 0.2.19 (2021-11-29 / 6c8e226)

## Fixed

- Fix issue where girouette tokens were not being applied to child elements. [See Github Issue](https://github.com/lambdaisland/ornament/issues/5)

## Changed

- Bump Girouette to 0.0.6

# 0.1.12 (2021-10-25 / d0a739b)

## Changed

- Bump Girouette to 0.0.5

# 0.0.7 (2021-10-01 / 52aa304)

## Added

- Initial implementation
