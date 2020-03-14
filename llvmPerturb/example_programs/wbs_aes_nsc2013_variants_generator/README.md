# Compilation

This whitebox is one whitebox in a series of multiple whiteboxes with various implemented encodings. It consists of two parts. a generator and an encryption engine  

1. Compile ...box_noenc_generator.c
2. Compile ...box_noenc.c
3. Execute ...box_noenc_generator to generate necessary lookup tables. Lookup tables gets saved in the current session memory.
4. To obtain ciphertext, execute ...box_noenc 74657374746573747465737474657374
