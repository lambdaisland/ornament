# Unreleased

## Added

## Fixed

## Changed

# 1.17.150 (2025-10-08 / 3c39b56)

## Fixed

- Make CSSProps implement print-dup, so they can be AOTd

# 1.16.141 (2025-04-29 / 8c00784)

## Changed

- Only include compiled CSS in cljs docstrings when the cljs optimization level
  is `:none`

# 1.15.138 (2025-04-24 / 8299d3c)

## Fixed

- Add a require-macros so defstyled can be referred from cljs directly

# 1.14.134 (2025-04-24 / dadcb61)

## Fixed

- Deal with more edge cases when referencing tokens inside style rules

# 1.13.130 (2025-04-16 / 83c295f)

## Changed

- [BREAKING] When setting a custom `:ornament/prefix` on the namespace, the
  separator `__` is no longer implied, to get the same result add `__` to the
  end of your prefix string.

## Added

- Support docstrings, they come after the tagname, before any styles or tokens
- If there's only a zero-arg render function (fn-tail), also emit a one-arg
  version that takes HTML attributes to be merged in.
- Add `defrules`, for general garden CSS rules
- Add `defprop`, for CSS custom properties (aka variables)
- Add `defutil`, for standalone utility classes
- Add `import-tokens!`, for importing W3C design token JSON files as properties (as per `defprop`)
- Allow setting metadata on a child list, useful for reagent/react keys

## Fixed

- Fix `defined-garden`
- Use of `defrules` in pure-cljs namespaces
- Fix implementation of ILookup on cljs

# 1.12.107 (2023-09-27 / 2444e34)

## Fixed

- Fix component resolution inside a set (in a rule of another component) (see tests for example)

# 1.11.101 (2023-09-13 / 213279d)

## Fixed

- Allow reusing the styles of one component directly inside another (see tests for example)

# 1.10.94 (2023-08-30 / d1e1c3b)

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
