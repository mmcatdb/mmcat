.PHONY: antlr-generate

# Generate an up-to-date parset, lexer and visitor from the Querycat.g4 grammar
antlr-generate:
    antlr4 -Dlanguage=Java -visitor -o ../java/cz/matfyz/querying/parsing antlr_generated -Xexact-output-dir -Werror Querycat.g4