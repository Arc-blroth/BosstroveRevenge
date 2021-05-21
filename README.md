# Bosstrove's Revenge (The Old Version)

Hey there! This is the "old" Bosstrove's Revenge, made between 2019 and 2020.
It implemented a basic game engine that could render to the console, OpenGL, and JavaFX,
but was abandoned due to both a lack of direction and issues with its core design.

Since writing the original Bosstrove's Revenge, I've come a long way in terms of programming.
Some of the major mistakes here include
- an overcomplicated init phase that involved unneccessary classloading
- using lists over arrays in cases where the list was guaranteed to never expand
- using JSON for all data, even in places where binary would be far more efficent
  - not using POJOs to represent that JSON and instead writing spaghetti code loaders
- unneccessary factory and registry design patterns

This branch serves as an archive of the code for the "original" Bosstrove's Revenge, as well
as an archive of the blunders and the fun I made while making it.

**You have been warned: concerning code lies ahead!**

---

A top-down 2d adventure game, dedicated to my AP Java teacher.