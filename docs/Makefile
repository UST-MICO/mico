# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line.
SPHINXOPTS    =
SPHINXBUILD   = sphinx-build
SPHINXPROJ    = MICODeveloperDocumentation
SOURCEDIR     = .
BUILDDIR      = _build
JAVASPHINX    = javasphinx-apidoc
JAVASOURCE    = ../mico-core/src/main/java
JAVADOCOUT    = ./mico-core/java

# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile

clean-javadoc:
	rm -rf $(JAVADOCOUT)

javadoc: clean-javadoc
	@$(JAVASPHINX) --update --output-dir="$(JAVADOCOUT)" "$(JAVASOURCE)"


# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile javadoc
	@$(SPHINXBUILD) -M $@ "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)
