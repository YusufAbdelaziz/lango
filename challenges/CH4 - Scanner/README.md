# 4- Scanner

1. **The lexical grammars of Python and Haskell are not regular. What does that mean, and why aren’t they?**

- As in regular languages, you need to match the characters using regular expressions. So in case of an opening and closing curly brackets, you only need to match the characters themselves without caring whether there's a space before a brace or not. In case of Python or Haskell, these have indentation sensitive syntax so they need to recognize the change of indentation level as tokens, which requires counting the space or indentation tokens which means more memory, and that is not available in a regular language.

- You may check **Scanner.java** and you can see that we omit the whitespaces while scanning the source file for building tokens.

---

2. **Aside from separating tokens—distinguishing print foo from printfoo—spaces aren’t used for much in most languages. However, in a couple of dark corners, a space does affect how code is parsed in CoffeeScript, Ruby, and the C preprocessor. Where and what effect does it have in each of those languages?**

<details>
<summary>CoffeeScript</summary>
<br>
- In CoffeeScript, consider the following code snippet :

```coffeescript

if eachController.indexOf("Controller.js") isnt -1
controller = require(controllersFolderPath + eachControllerName)
controller.register server

```

Gets compiled into this JS code

```javascript
if (eachController.indexOf("Controller.js") !== -1) {
  controller = require(controllersFolderPath + eachControllerName);
  controller.register(server);
}
```

But adding a space between `indexOf` and the brace would change how the code gets compiled.

So let's consider this case :

```coffeescript
  if eachController.indexOf ("Controller.js") isnt -1
    controller = require(controllersFolderPath + eachControllerName)
    controller.register server
```

Which would be compiled into :

```javascript
if (eachController.indexOf("Controller.js" !== -1)) {
  controller = require(controllersFolderPath + eachControllerName);
  controller.register(server);
}
```

So CoffeeScript wraps the whole `("Controller.js") isnt -1` expression with braces and therefore compiled as an argument to `indexOf` function. So spaces are critical in CoffeeScript.

</details>

<details>
<summary>Ruby</summary>
<br>
- A method in Ruby can run with or without a parentheses. In case of running a method without parentheses, you can pass the arguments after adding a space after the method's name.
  
<br>

So consider `Array.new 1,2` which is equivalent to `Array.new(1,2)`, but adding a space after `new` would make `(1,2)` as a tuple instead of separated values because Ruby expects the argument of a method after the space.

</details>

<details>
<summary>C Preprocessor</summary>
<br>
- Spaces are used to distinguish between a marco and a function-like macro.
  
<br>

So for example :

```c
  #define myMacro (p) (p)
  #define macroFun(p) (p)
```

The first is a marco with replacement list whereas the second is a function-like macro that takes a parameter and expands it into `p` which is the parameter.

</details>

---

3. **Our scanner here, like most, discards comments and whitespace since those aren’t needed by the parser. Why might you want to write a scanner that does not discard those? What would it be useful for?**

- In case of whitespaces, it's crucial not to discard those because we may want to implement a language that is similar to Python or Haskell where indentation level should be somehow stored (we may not store the whitespaces themselves but the indentation level).
- In case of comments, it's useful to store those because we may have a documentation generator (similar to Javadoc or PHPDoc) that generates a styled docs using those comments.
