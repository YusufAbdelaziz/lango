# Start from the root folder (project's).

# Redirect compiled files to classes folder, remember to add it to gitignore.
cd src
javac -d ./classes jlox/main/JLox.java 

# Use this to fire the REPL
java -cp ./classes jlox.main.JLox

cd ..