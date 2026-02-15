# CalculatorSuite

A comprehensive mathematical graphing and visualization application built with Compose Multiplatform, targeting desktop platforms with native performance optimizations.

## Overview

CalculatorSuite is an advanced mathematical plotting application that provides real-time visualization of various mathematical expressions including Cartesian functions, polar equations, parametric curves, vectors, points, arcs, and geometric areas. The application combines a modern user interface built with Jetpack Compose with high-performance native C++ computation for complex implicit function plotting.

## Key Features

### Expression Support

The application supports a wide range of mathematical expressions:

- **Cartesian Coordinates**
  - Explicit functions: `y = f(x)` (e.g., `y = x^2`, `y = sin(x)`)
  - Vertical lines: `x = c` (constant values)
  - Implicit equations: `f(x,y) = 0` (e.g., `x^2 + y^2 = 25`)

- **Polar Coordinates**
  - Polar functions: `r = f(u)` (e.g., `r = sin(u)`)
  - Radial lines: `u = c` (constant angles)

- **Parametric Equations**
  - Parametric curves: `r(t) = (x(t), y(t))`

- **Geometric Objects**
  - Points: `(x, y)`
  - Vectors: `vec(x, y)`
  - Arcs: `arc((x, y), r, start, sweep)`
  - Areas: `[(x1, y1), (x2, y2), ...]`

- **Calculus Operations**
  - Numerical integration: `Integral(f(x), a, b)`

### Core Functionality

- **Multi-Function Plotting**: Plot multiple mathematical expressions simultaneously with individual color customization
- **Interactive Canvas**: Pan, zoom, and navigate through the coordinate plane with smooth gestures
- **Real-time Rendering**: Instant visualization updates as expressions are modified
- **Color Customization**: Circular color picker for assigning distinct colors to each function
- **Expression Detection**: Automatic detection and parsing of expression types
- **Native Performance**: C++ backend for computationally intensive implicit function plotting

### User Interface

- **Side Panel**: Collapsible interface for function management
  - Add new functions through dialog interface
  - Color selection for each function
  - Select and drag modes for interaction
  - Function list with inline editing

- **Graph Dialog**: Comprehensive dialog for adding various graph types
  - Type selection: Function, Point, Vector, Arc, Area
  - Dynamic input fields based on selected type
  - Form validation and structured input

- **Cartesian Grid Canvas**: Professional coordinate system with:
  - Adjustable grid lines and axis labels
  - Origin markers
  - Scale indicators
  - Coordinate tracking

## Technical Architecture

### Technology Stack

- **UI Framework**: Compose Multiplatform (Desktop/JVM)
- **Language**: Kotlin
- **Native Library**: C++ with ExprTK for mathematical expression evaluation
- **State Management**: Custom ViewModel with MVI architecture
- **Native Interop**: JNA (Java Native Access) for C++ library integration
- **Build System**: Gradle with Kotlin DSL

### Project Structure

```
CalculatorSuite/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/
│   │   │   │   └── org/calculator/
│   │   │   │       ├── ui/
│   │   │   │       │   ├── components/    # Reusable UI components
│   │   │   │       │   ├── screens/       # Main screens and ViewModels
│   │   │   │       │   ├── theme/         # Material Design theme
│   │   │   │       │   └── utils/         # UI utilities
│   │   │   │       ├── utils/             # Math utilities
│   │   │   │       ├── nativeLib/         # Native library interfaces
│   │   │   │       └── App.kt             # Application entry point
│   │   │   ├── cpp/                       # C++ native implementation
│   │   │   │   ├── implicit_graph_core.cpp
│   │   │   │   ├── implicit_graph_core.h
│   │   │   │   ├── exprtk.hpp             # Expression evaluation library
│   │   │   │   └── CMakeLists.txt
│   │   │   └── composeResources/          # Application resources
│   │   ├── jvmMain/                       # JVM-specific implementations
│   │   └── webMain/                       # Web target (future support)
│   └── build.gradle.kts
├── gradle/
├── settings.gradle.kts
└── README.md
```

### Native Library Integration

The application uses a custom C++ library (`implicit_graph`) for high-performance implicit function plotting:

1. **ExprTK Integration**: Utilizes the ExprTK C++ Mathematical Expression Parsing and Evaluation Library for robust formula evaluation
2. **JNA Bridge**: Java Native Access provides seamless Kotlin-to-C++ interoperability without JNI boilerplate
3. **Bitmap Evaluation**: Efficient pixel-by-pixel evaluation of implicit equations
4. **Multi-formula Support**: Capability to evaluate and compare multiple formulas simultaneously

### State Management

The application implements the Model-View-Intent (MVI) architectural pattern:

- **State**: Immutable data class containing all UI state
- **Events**: User actions and system events
- **Effects**: One-time side effects
- **ViewModel**: Processes events and updates state

### Mathematical Utilities

The `MathUtils` module provides:

- Coordinate system transformations (Cartesian ↔ Canvas)
- Adaptive numerical integration using Simpson's rule
- Expression type detection and parsing
- Geometric calculations (arc rendering, vector operations)
- Area computation and polygon handling

## Building and Running

### Prerequisites

- JDK 17 or higher
- CMake 3.20+ (for native library compilation)
- Visual Studio 2022 (Windows) with C++ build tools
- Gradle 8.0+ (included via wrapper)

### Desktop Application (Windows)

#### Development Mode

```shell
.\gradlew.bat :composeApp:run
```

#### Native Library Compilation

The native C++ library is automatically built and copied during the Gradle build process:

1. CMake generates Visual Studio project files
2. C++ code is compiled to `implicit_graph.dll`
3. DLL is copied to JVM resources directory

To manually trigger native library build:

```shell
.\gradlew.bat buildNativeRelease
```

### Web Application (Experimental)

#### WebAssembly (Wasm)

```shell
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
```

#### JavaScript

```shell
.\gradlew.bat :composeApp:jsBrowserDevelopmentRun
```

## Usage

### Adding Functions

1. Click the menu icon to open the side panel
2. Click the "+" icon to open the Add Graph dialog
3. Select the type of graph (Function, Point, Vector, Arc, or Area)
4. Enter the required parameters
5. Click "CONFIRM" to add the graph

### Customizing Colors

1. Click the function field to select it
2. Use the circular color picker to choose a color
3. Click "Confirm" to apply the color

### Interacting with the Canvas

- **Pan**: Drag the canvas to move the viewport
- **Zoom**: Use scroll wheel to zoom in/out (0.1x - 10x range)
- **Reset**: Double-click to reset view to default

### Expression Syntax Examples

- Explicit function: `y = x^2 + 3*x - 2`
- Trigonometric: `y = sin(2*x) + cos(x)`
- Implicit circle: `x^2 + y^2 = 25`
- Polar rose: `r = sin(3*u)`
- Parametric curve: `r(t) = (cos(t), sin(t))`
- Point: `(3, 4)`
- Vector: `vec(2, 5)`
- Arc: `arc((0, 0), 5, 0, 90)`

## Architecture Highlights

### Component Overview

- **MultiCanvas**: Main rendering component that orchestrates all graph types
- **CartesianGridCanvas**: Draws the coordinate system, grid lines, and axes
- **AddGraphDialog**: Modal dialog for structured input of various graph types
- **FunctionField**: Input field component with action buttons and color indicators
- **CircularColorPicker**: Custom color selection component
- **ImplicitPlotter**: Native library wrapper for implicit function evaluation

### Performance Optimizations

- Native C++ backend for computationally expensive operations
- Efficient bitmap-based rendering for implicit functions
- Canvas-level optimizations with Compose drawing APIs
- Adaptive numerical integration for accurate integral calculations

## Development Roadmap

### Planned Features

- 3D graphing capabilities
- Export functionality (PNG, SVG, data points)
- Animation support for time-dependent functions
- Function analysis tools (derivatives, extrema, intersections)
- Additional coordinate systems (spherical, cylindrical)
- Symbolic computation integration
- Enhanced mobile platform support

### Known Limitations

- Implicit function plotting currently Windows-only (native library dependency)
- Web targets have limited mathematical evaluation capabilities
- Large-scale integral computation may impact performance

## Technical Notes

### Expression Evaluation

The C++ backend uses ExprTK, a powerful mathematical expression parsing library that supports:

- Standard mathematical functions (sin, cos, exp, log, etc.)
- Operators (+, -, *, /, ^, etc.)
- Variable substitution
- Complex nested expressions

### Coordinate Transformations

The application maintains precise coordinate mappings between:

1. Mathematical coordinate space (continuous)
2. Canvas pixel space (discrete)
3. Grid coordinate space (user-visible)

Transformations account for:
- Viewport offset and scaling
- Y-axis inversion (canvas vs. Cartesian convention)
- Grid step size and scale factor

## Acknowledgments

- **ExprTK**: Mathematical Expression Parsing and Evaluation Library by Arash Partow
- **Compose Multiplatform**: JetBrains UI framework
- **JNA**: Java Native Access library

---

Built with Compose Multiplatform and powered by native C++ computation for optimal performance.
