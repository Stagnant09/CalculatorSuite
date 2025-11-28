#include <jni.h>
#include <cmath>
#include <vector>
#include <string>
#include <algorithm>
#include <android/log.h> // Include for debugging logs
#include "exprtk.hpp"

// Logging macros for Android
#define LOG_TAG "ImplicitPlotting"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Use double for expression evaluation for precision
using real_t = double;
using symbol_table_t = exprtk::symbol_table<real_t>;
using expression_t = exprtk::expression<real_t>;
using parser_t = exprtk::parser<real_t>;

// 2. Global/Static State (Outside of any function)
static symbol_table_t symbol_table;
static expression_t expression;
static parser_t parser;
static real_t x_var;
static real_t y_var;
static std::string current_formula = "";

/**
 * @brief Parses and compiles the mathematical expression string.
 * This function also transforms an equation (LHS=RHS) into an expression (LHS - RHS).
 */
void setup_expression(const std::string& input_formula) {
    if (input_formula == current_formula) {
        return; // Already set up.
    }

    std::string formula_to_compile = input_formula;
    size_t eq_pos = formula_to_compile.find('=');

    if (eq_pos != std::string::npos) {
        // Equation found (LHS=RHS). Transform to implicit form (LHS - RHS).
        std::string lhs = formula_to_compile.substr(0, eq_pos);
        std::string rhs = formula_to_compile.substr(eq_pos + 1);

        // Remove leading/trailing spaces from RHS for a cleaner expression
        rhs.erase(0, rhs.find_first_not_of(" \t\n\r"));
        rhs.erase(rhs.find_last_not_of(" \t\n\r") + 1);

        formula_to_compile = lhs + " - (" + rhs + ")";
        LOGD("Transformed formula: %s", formula_to_compile.c_str());
    } else {
        // No equation found, assume it is already in f(x,y) form.
        LOGD("Compiling expression: %s", formula_to_compile.c_str());
    }

    // 1. Clear and setup symbol table
    symbol_table.clear();
    x_var = 0.0;
    y_var = 0.0;
    symbol_table.add_variable("x", x_var);
    symbol_table.add_variable("y", y_var);
    symbol_table.add_constants();

    // 2. Register symbol table with expression
    expression.register_symbol_table(symbol_table);

    // 3. Parse and compile the expression
    bool successful_parse = parser.compile(formula_to_compile, expression);

    if (successful_parse) {
        current_formula = input_formula; // Store the original input formula
        LOGD("Parsing successful.");
    } else {
        current_formula = "";
        // This log will show if the transformed formula is still failing to compile.
        LOGD("Parsing FAILED for formula: %s", formula_to_compile.c_str());
    }
}

/**
 * @brief FAST function to evaluate the already compiled expression.
 * This is called many times inside the drawing loop.
 */
float evaluate_function(double x, double y) {
    // 1. Update the input variables in the symbol table
    // Since 'x_var' and 'y_var' are bound by reference, updating them
    // immediately updates the symbols used by the expression object.
    x_var = x;
    y_var = y;

    // 2. Evaluate the compiled expression (This is the fast part)
    // Note: 'expression.value()' returns the result of the compiled formula.
    return (float)expression.value();
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_calculator_native_NativePlot_computeImplicit(
        JNIEnv* env,
        jobject /* this */,
        jint width,
        jint height,
        jfloat originX,
        jfloat originY,
        jfloat step,
        jfloat scale,
        jfloat threshold,
        jstring formula // <--- JNI string containing the formula
) {
    // 1. Convert jstring to C++ string
    const char* formula_chars = env->GetStringUTFChars(formula, nullptr);
    std::string formula_cpp(formula_chars);
    env->ReleaseStringUTFChars(formula, formula_chars);

    // 2. Parse the expression ONCE (and only if it has changed)
    setup_expression(formula_cpp);

    // If parsing failed, you might return an empty array or an error indicator.
    if (current_formula.empty()) {
        // Log error and return an empty graph
        return env->NewIntArray(width * height);
    }

    // Output pixel bitmap (1 = draw, 0 = empty)
    std::vector<jint> bitmap(width * height, 0);

    const float pixelStep = std::max(1.1f / scale, 1.0f);

    for (int px = 0; px < width; px += pixelStep) {
        for (int py = 0; py < height; py += pixelStep) {

            // Convert to world coordinates
            double worldX = (px - originX) / (step * scale);
            double worldY = (originY - py) / (step * scale);

            // 3. FAST EVALUATION: Direct C++ call
            jfloat f = evaluate_function(worldX, worldY);

            if (std::fabs(f) < threshold) {
                bitmap[py * width + px] = 1;
            }
        }
    }

    // Convert to jintArray to send back to Kotlin
    jintArray result = env->NewIntArray(width * height);
    env->SetIntArrayRegion(result, 0, width * height, bitmap.data());
    return result;
}