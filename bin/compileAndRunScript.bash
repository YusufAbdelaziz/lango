# Start from the root folder (project's).

# Redirect compiled files to classes folder, remember to add it to gitignore.
cd src
javac -d ./classes lango/main/Lango.java

# Use this to fire the REPL
# java -cp ./classes jlox.main.JLox

# Use this to run code from a script file.
java -cp ./classes lango.main.Lango ../script.lox

cd ..
