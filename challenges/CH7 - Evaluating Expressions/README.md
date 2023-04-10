# 7 - Evaluating Expressions

1. **Allowing comparisons on types other than numbers could be useful. The operators might have a reasonable interpretation for strings. Even comparisons among mixed types, like 3 < "pancake" could be handy to enable things like ordered collections of heterogeneous types. Or it could simply lead to bugs and confusion. <br/> <br/>Would you extend Lox to support comparing other types? If so, which pairs of types do you allow and how do you define their ordering? Justify your choices and compare them to other languages.**

- In Python3, it's possible to compare between strings in case of sorting a list of strings. The list is sorted lexicographically.
- It's also possible to sort a list of Booleans and Numbers, since booleans are subclasses of numbers (1 for true, 0 for false).
- Extending Lox to compare between strings for sorting is reasonable but would be buggy for mixed types like strings and numbers. Comparison like `3 < "pancake"` would lead to a confusion.

---

2. **Many languages define + such that if either operand is a string, the other is converted to a string and the results are then concatenated. For example, "scone" + 4 would yield scone4. Extend the code in visitBinaryExpr() to support that.**

- It's implemented now, check the `PLUS` case at `visitBinaryExpr`.

---

3. **What happens right now if you divide a number by zero? What do you think should happen? Justify your choice. How do other languages you know handle division by zero, and why do they make the choices they do? <br/> <br/>Change the implementation in visitBinaryExpr() to detect and report a runtime error for this case.**

- Currently, it returns "Infinity".
- I think we should throw a runtime exception because doing arithmetic operations on infinity would screw up the operations.
- Taking Python3 as an example, dividing by zero raises `ZeroDivisionError`.
- Meanwhile in Java, you can't divide an integer zero (0 not 0.0) as such values like (Infinity, -Infinity, or NaN) don't exist. Whereas if one of the operands was a double value, the IEEE 754 floating-point specifications have specific values for Infinity, -Infinity, and NaN. So I think Java is just following the specifications.
- I've added a condition in the `SLASH` case at `visitBinaryExpr` so that division by zero throws a `RuntimeError`.
