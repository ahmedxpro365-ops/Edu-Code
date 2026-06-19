package com.educode.app.features.codeeditor

enum class ProgrammingLanguage(val extension: String, val displayName: String) {
    HTML(".html", "HTML"),
    CSS(".css", "CSS"),
    JAVASCRIPT(".js", "JavaScript"),
    PYTHON(".py", "Python"),
    C(".c", "C"),
    CPP(".cpp", "C++"),
    CSHARP(".cs", "C#"),
    JAVA(".java", "Java"),
    PHP(".php", "PHP"),
    RUST(".rs", "Rust")
}

object LangTemplates {
    fun getTemplate(lang: ProgrammingLanguage): String {
        return when (lang) {
            ProgrammingLanguage.HTML -> """<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <title>مرحباً بك في Edu Code</title>
    <style>
        body {
            background-color: #0f121e;
            color: #00e5ff;
            text-align: center;
            font-family: sans-serif;
            margin-top: 100px;
        }
        h1 {
            text-shadow: 0 0 10px #6b21a8;
        }
    </style>
</head>
<body>
    <h1>محرر الأكواد الاحترافي</h1>
    <p>البداية البرمجية الرائعة!</p>
    <script>
        console.log("تم تشغيل قالب الويب الخارق بنجاح!");
    </script>
</body>
</html>"""

            ProgrammingLanguage.CSS -> """/* Cyberpunk Premium Stylesheet */
body {
    background-color: #0f121e;
    color: #ffffff;
    font-size: 16px;
}

.glow-card {
    border: 2px solid #00e5ff;
    box-shadow: 0 0 15px rgba(0, 229, 255, 0.6);
    border-radius: 12dp;
    padding: 20px;
    transition: all 0.3s ease;
}

.glow-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 0 25px #6b21a8;
}"""

            ProgrammingLanguage.JAVASCRIPT -> """// JavaScript Algorithm Template
function findPrimes(max) {
    const primes = [];
    for (let i = 2; i <= max; i++) {
        let isPrime = true;
        for (let j = 2; j <= Math.sqrt(i); j++) {
            if (i % j === 0) {
                isPrime = false;
                break;
            }
        }
        if (isPrime) primes.push(i);
    }
    return primes;
}

const primesList = findPrimes(50);
console.log("الأعداد الأولية حتى 50 هي:");
console.log(primesList.join(", "));"""

            ProgrammingLanguage.PYTHON -> """# Python Algorithm - Fibonacci sequence
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n - 1) + fibonacci(n - 2)

# Generate first 10 Fibonacci numbers
limit = 10
print(f"أول {limit} أرقام من متتالية فيبوناتشي:")
for i in range(limit):
    print(f"العدد {i + 1}: {fibonacci(i)}")
"""

            ProgrammingLanguage.C -> """#include <stdio.h>

// C Language Standard template
int main() {
    int max = 5;
    printf("مرحباً بك في عالم لغة C!\n");
    
    for (int i = 0; i < max; i++) {
        printf("العداد: %d\n", i + 1);
    }
    
    return 0;
}"""

            ProgrammingLanguage.CPP -> """#include <iostream>
#include <vector>
#include <string>

using namespace std;

// C++ Modern template
int main() {
    vector<string> stackNames = {"HTML", "CSS", "JS", "C++", "Python"};
    
    cout << "--- محب برجمة الـ C++ ---" << endl;
    for (const string& tech : stackNames) {
        cout << "أنا أتعلم: " << tech << endl;
    }
    
    return 0;
}"""

            ProgrammingLanguage.CSHARP -> """using System;

namespace EduCodeEditor
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("C# Premium Code Editor Enabled.");
            int levels = 5;
            for (int i = 1; i <= levels; i++) {
                string stars = new string('*', i);
                Console.WriteLine(stars);
            }
        }
    }
}"""

            ProgrammingLanguage.JAVA -> """import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("مرحباً بك في لغة Java داخل Edu Code!");
        
        ArrayList<String> lessons = new ArrayList<>();
        lessons.add("المتغيرات (Variables)");
        lessons.add("الشروط (Conditions)");
        lessons.add("الحلقات (Loops)");
        
        for (int i = 0; i < lessons.size(); i++) {
            System.out.println((i + 1) + ". " + lessons.get(i));
        }
    }
}"""

            ProgrammingLanguage.PHP -> """<?php
// PHP Core Script template
class CodePresenter {
    private ${'$'}name;
    
    public function __construct(${'$'}name) {
        ${'$'}this->name = ${'$'}name;
    }
    
    public function greet() {
        return "أهلاً بك يا " . ${'$'}this->name . "، مبرمج الويب الخارق!";
    }
}

${'$'}presenter = new CodePresenter("أحمد");
echo ${'$'}presenter->greet() . "\n";
?>"""

            ProgrammingLanguage.RUST -> """// Rust Safe System Template
fn main() {
    let name = "بَرموج الذكي";
    println!("مرحباً بكم في لغة Rust الآمنة والسريعة!");
    
    // Pattern Matching
    match name {
        "بَرموج الذكي" => println!("المطور المساعد متاح لتفسير هذا الكود!"),
        _ => println!("مرحباً بك يا صديقي!"),
    }
}"""
        }
    }

    fun getSuggestions(lang: ProgrammingLanguage): List<String> {
        return when (lang) {
            ProgrammingLanguage.HTML -> listOf("<div>", "</div>", "<p>", "</p>", "<span>", "</span>", "class=\"\"", "id=\"\"", "href=\"\"", "<img>", "<script>", "</script>", "<style>", "</style>", "<ul>", "<li>", "<table>")
            ProgrammingLanguage.CSS -> listOf("color:", "background-color:", "margin:", "padding:", "border-radius:", "box-shadow:", "font-size:", "display: flex;", "justify-content:", "align-items:", "width:", "height:", "position:")
            ProgrammingLanguage.JAVASCRIPT -> listOf("const", "let", "function", "return", "console.log(", "if (", "else", "for (let i = 0;", "while (", "forEach(", "map(", "filter(", "document.getElementById(", "addEventListener(")
            ProgrammingLanguage.PYTHON -> listOf("def ", "return ", "print(", "if ", "elif ", "else:", "for i in range(", "while ", "import ", "from ", "class ", "try:", "except Exception as e:")
            ProgrammingLanguage.C -> listOf("#include <stdio.h>", "int main() {", "printf(", "scanf(", "return 0;", "if (", "for (int i =", "while (", "switch(", "case ", "break;")
            ProgrammingLanguage.CPP -> listOf("#include <iostream>", "using namespace std;", "int main() {", "cout <<", "endl;", "cin >>", "vector<", "string", "return 0;", "for (", "while (")
            ProgrammingLanguage.CSHARP -> listOf("using System;", "namespace ", "class ", "static void Main(string[] args)", "Console.WriteLine(", "Console.ReadLine();", "int ", "string ", "for (", "foreach (var ")
            ProgrammingLanguage.JAVA -> listOf("public class Main {", "public static void main(String[] args)", "System.out.println(", "int ", "String ", "for (int i =", "ArrayList<", "if (", "return ;")
            ProgrammingLanguage.PHP -> listOf("<?php", "echo ", "function ", "class ", "public function ", "${'$'}this->", "new ", "foreach (", "as ", "return ", "require_once", "?>")
            ProgrammingLanguage.RUST -> listOf("fn main() {", "println!(", "let mut ", "let ", "match ", "struct ", "impl ", "return ", "if ", "for i in ", "vec!", "Option<", "Result<", "unwrap()")
        }
    }
}
