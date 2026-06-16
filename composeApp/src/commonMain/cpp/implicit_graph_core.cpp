#include "implicit_graph_core.h"
#include "exprtk.hpp"
#include <cstdint>
#include <string>
#include <vector>
#include <cmath>
#include <algorithm>
#ifdef _WIN32
#define DLL_EXPORT __declspec(dllexport)
#else
#define DLL_EXPORT
#endif

// types from exprtk
using real_t = double;
using symbol_table_t = exprtk::symbol_table<real_t>;
using expression_t = exprtk::expression<real_t>;
using parser_t = exprtk::parser<real_t>;

// global state (single instance for simplicity)
static symbol_table_t symbol_table;
static expression_t expression;
static parser_t parser;
static real_t x_var = 0.0;
static real_t y_var = 0.0;
static std::string current_formula = "";

static symbol_table_t symbol_table2;
static expression_t expression2;
static parser_t parser2;
static real_t x_var2 = 0.0;
static real_t y_var2 = 0.0;
static std::string current_formula2 = "";

static void setup_expression_internal(const std::string& input_formula) {
    if (input_formula == current_formula) return;

    std::string formula_to_compile = input_formula;
    size_t eq_pos = formula_to_compile.find('=');
    if (eq_pos != std::string::npos) {
        std::string lhs = formula_to_compile.substr(0, eq_pos);
        std::string rhs = formula_to_compile.substr(eq_pos + 1);
        rhs.erase(0, rhs.find_first_not_of(" \t\n\r"));
        rhs.erase(rhs.find_last_not_of(" \t\n\r") + 1);
        formula_to_compile = lhs + " - (" + rhs + ")";
    }

    symbol_table.clear();
    x_var = 0.0;
    y_var = 0.0;
    symbol_table.add_variable("x", x_var);
    symbol_table.add_variable("y", y_var);
    symbol_table.add_constants();

    expression.register_symbol_table(symbol_table);
    bool successful_parse = parser.compile(formula_to_compile, expression);
    if (successful_parse) {
        current_formula = input_formula;
    } else {
        current_formula.clear();
    }
}

static void setup_expression_internal2(const std::string& input_formula) {
    if (input_formula == current_formula2) return;

    std::string formula_to_compile = input_formula;
    size_t eq_pos = formula_to_compile.find('=');
    if (eq_pos != std::string::npos) {
        std::string lhs = formula_to_compile.substr(0, eq_pos);
        std::string rhs = formula_to_compile.substr(eq_pos + 1);
        rhs.erase(0, rhs.find_first_not_of(" \t\n\r"));
        rhs.erase(rhs.find_last_not_of(" \t\n\r") + 1);
        formula_to_compile = lhs + " - (" + rhs + ")";
    }

    symbol_table2.clear();
    x_var2 = 0.0;
    y_var2 = 0.0;
    symbol_table2.add_variable("x", x_var2);
    symbol_table2.add_variable("y", y_var2);
    symbol_table2.add_constants();

    expression2.register_symbol_table(symbol_table2);
    bool successful_parse = parser2.compile(formula_to_compile, expression2);
    if (successful_parse) {
        current_formula2 = input_formula;
    } else {
        current_formula2.clear();
    }
}

static float evaluate_function(double x, double y) {
    x_var = x;
    y_var = y;
    return static_cast<float>(expression.value());
}

static float evaluate_function2(double x, double y) {
    x_var2 = x;
    y_var2 = y;
    return static_cast<float>(expression2.value());
}

extern "C" {

DLL_EXPORT void ig_set_formula(const char* formula) {
    if (!formula) {
        current_formula.clear();
        return;
    }
    setup_expression_internal(std::string(formula));
}

DLL_EXPORT void ig_set_formula2(const char* formula) {
    if (!formula) {
        current_formula2.clear();
        return;
    }
    setup_expression_internal2(std::string(formula));
}

DLL_EXPORT int ig_evaluate_bitmap(
    int32_t width,
    int32_t height,
    float originX,
    float originY,
    float step,
    float scale,
    float threshold,
    int32_t* outBitmap
) {
    if (!outBitmap) return -1;
    if (current_formula.empty()) return -2; // formula not set / compile failed
    
    // Improved sampling: we evaluate every pixel if threshold is small or zoom is high
    // The previous pixelStep logic was causing dotted lines.
    
    for (int py = 0; py < height; ++py) {
        for (int px = 0; px < width; ++px) {
            double worldX = (px - originX) / (step * scale);
            double worldY = (originY - py) / (step * scale);
            float f = evaluate_function(worldX, worldY);
            if (std::fabs(f) < threshold) {
                outBitmap[py * width + px] = 1;
            } else {
                outBitmap[py * width + px] = 0;
            }
        }
    }

    return 0;
}

DLL_EXPORT int ig_meet_points_of_f1_f2(int32_t width, int32_t height, float originX, float originY, float step, float scale, float threshold, int32_t* outBitmap) {
    if (!outBitmap) return -1;
    if (current_formula.empty() || current_formula2.empty()) return -2; // formula not set / compile failed

    for (int py = 0; py < height; ++py) {
        for (int px = 0; px < width; ++px) {
            double worldX = (px - originX) / (step * scale);
            double worldY = (originY - py) / (step * scale);
            float f = evaluate_function(worldX, worldY);
            float f2 = evaluate_function2(worldX, worldY);
            if (std::fabs(f) < threshold && std::fabs(f2) < threshold) {
                outBitmap[py * width + px] = 1;
            } else {
                outBitmap[py * width + px] = 0;
            }
        }
    }

    return 0;
}

} // extern "C"
