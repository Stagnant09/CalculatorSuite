#include "implicit_graph_core.hpp"
#include "exprtk.hpp"
#include <string>
#include <vector>
#include <cmath>
#include <algorithm>

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

static float evaluate_function(double x, double y) {
    x_var = x;
    y_var = y;
    return static_cast<float>(expression.value());
}

extern "C" {

void ig_set_formula(const char* formula) {
    if (!formula) {
        current_formula.clear();
        return;
    }
    setup_expression_internal(std::string(formula));
}

int ig_evaluate_bitmap(
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
    std::vector<int32_t> local(width * height, 0);
    const float pixelStep = std::max(1.1f / scale, 1.0f);

    for (int px = 0; px < width; px += static_cast<int>(pixelStep)) {
        for (int py = 0; py < height; py += static_cast<int>(pixelStep)) {
            double worldX = (px - originX) / (step * scale);
            double worldY = (originY - py) / (step * scale);
            float f = evaluate_function(worldX, worldY);
            if (std::fabs(f) < threshold) {
                local[py * width + px] = 1;
            }
        }
    }

    // copy to caller buffer
    std::copy(local.begin(), local.end(), outBitmap);
    return 0;
}

} // extern "C"
