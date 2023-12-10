# Starting from the root

cd src
echo "Compiling GenerateAst.java"
javac tools/GenerateAst.java

echo "Executing GenerateAst"
java tools.GenerateAst "../src/lango/astNodes"

cd ..
