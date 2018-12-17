# Developer Documentation

Documentation: [readthedocs](https://mico-dev.readthedocs.io)

![build badge](https://readthedocs.org/projects/mico-dev/badge/?version=latest)


## Useful links:

 *  [Sphinx](http://www.sphinx-doc.org/en/master/)

    Sphinx is a tool to compile ReStructuredText documentation into a variety of formats.
 *  [ReStructuredText](http://www.sphinx-doc.org/en/master/usage/restructuredtext/basics.html)
 *  [Getting started (readthedocs)](https://docs.readthedocs.io/en/latest/intro/getting-started-with-sphinx.html#using-markdown-with-sphinx)


## Build the documentation locally:

**Install Graphviz**

Using apt (Ubuntu / Debian):

```bash
sudo apt-get install graphviz
```

Using brew (Mac OS X):

```bash
sudo brew install graphviz
```

**Upgrade pip:**

```bash
sudo -H pip2 install --upgrade pip
sudo -H pip3 install --upgrade pip
```

**Install Typedoc**

```bash
npm install -g typedoc
```

**Install requirements:**

```bash
pip install -r requirements.txt
```

Make sure you have the `dot` command from `graphviz`, `typedoc` and a basic `LaTeX` environment in your path!

**Update Javadoc:**

```bash
javasphinx-apidoc --update --output-dir="./mico-core/java" "../mico-core/src/main/java"
```


**Build html:**

```bash
make html
```

open `_build/html/index.html` in your browser

If the typescript documentation has changed please build the documentation locally and commit the new `typedoc.json`!


## Enabled Extensions:

 *  sphinx.ext.intersphinx
 *  sphinx.ext.autosectionlabel
 *  sphinx.ext.todo
 *  sphinx.ext.imgmath
 *  sphinx.ext.graphviz
 *  [sphinx_js](https://github.com/erikrose/sphinx-js)
 *  [javasphinx](https://bronto-javasphinx.readthedocs.io/en/latest/)
