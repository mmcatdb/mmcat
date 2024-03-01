---
title: "Frontend Application"
weight: 30
---

The application is located in the `example-ui` package. It is based on the [Vue 3](https://vuejs.org/) framework with [Vite](https://vitejs.dev/), TypeScript and Options API.

## Architecture

The main purpose of the application is to provide tools for the definition and execution of transformation jobs. Because of this it is a single-page application with client-side rendering and internal router.

Vue is based on the Model-View-ViewModel pattern. The whole application is hierarchically divided to components, each of which is basically a tiny instance of an MVVM application.

### MVVM

The model consists of data and business logic, and it is the source of truth of the application. The view-model (also called binder) controls what should be provided to the view. Whenever it receives an input from the view, it processes it and updates the model accordingly. The view presents the data to the user. It also captures user inputs and sends them to the view-model.

### Vue

The important part is that the binding between the view and the view-model is done by the framework. Whenever a variable changes, Vue automatically re-renders the corresponding part of the displayed view. Also, whenever a user changes an input, Vue sets the respective variable to the new value.

This behavior is similar to other popular web frameworks, i.e. React. The difference here is that while React uses immutable data structures and always re-renders the whole view, Vue reacts on the mutations of the data. Then it finds out what exactly changed and re-renders only what is necessary.

### Components

A component consists of script, template and optionally some CSS styles. The script defines the state of the component and its methods. The template is an HTML structure enhanced with conditions, loops and variables that binds to the state in the script. It can also contain other components (or itself thanks to recursion).

The information flows from the parent components to their children via the so-called props. The binding is again handled by the framework. The children can propagate changes back to their parents by events.
