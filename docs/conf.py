# -*- coding: utf-8 -*-
#
# Configuration file for the Sphinx documentation builder.
#
# This file does only contain a selection of the most common options. For a
# full list see the documentation:
# http://www.sphinx-doc.org/en/master/config

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
# import os
# import sys
# sys.path.insert(0, os.path.abspath('.'))
import os
from recommonmark.parser import CommonMarkParser
from recommonmark.transform import AutoStructify

on_rtd = os.environ.get('READTHEDOCS') == 'True'

# -- Recommonmark Monkey patch -----------------------------------------------

# https://github.com/rtfd/recommonmark/issues/93#issuecomment-433371240
from functools import wraps
from recommonmark.states import DummyStateMachine


old_run_role = DummyStateMachine.run_role
@wraps(old_run_role)
def run_role(self, name, *args, **kwargs):
    if name == 'doc':
        name = 'any'
    return old_run_role(self, name, *args, **kwargs)

DummyStateMachine.run_role = run_role

# -- sphinx-js Monkey patch --------------------------------------------------
from sphinx_js import doclets
from tempfile import NamedTemporaryFile
import subprocess
from os.path import relpath, join
from sphinx.errors import SphinxError
from errno import ENOENT
from json import load, dump


def analyze_typescript(abs_source_paths, app):
    command = doclets.Command('typedoc')
    if app.config.jsdoc_config_path:
        command.add('--tsconfig', app.config.jsdoc_config_path)

    json_path = '../docs/mico-admin/ts/typedoc.json'

    source = abs_source_paths[0]
    command.add('--exclude', '**/node_modules/**/*.*', '--json', json_path, '--ignoreCompilerErrors', *abs_source_paths)
    if not on_rtd:
        # only build typedoc json locally as readthedocs build container does not
        # support it natively (and typedoc process takes a while to finish)
        try:
            subprocess.call(command.make(), cwd=source)
            with open(json_path) as typedoc_json:
                typedoc = load(typedoc_json)

            def clean_paths(typedoc):
                """Make all paths relative to not leak path info to github."""
                if not isinstance(typedoc, dict):
                    return
                for key in typedoc:
                    if isinstance(typedoc[key], dict):
                        clean_paths(typedoc[key])
                    if isinstance(typedoc[key], list):
                        for entry in typedoc[key]:
                            clean_paths(entry)
                    if key == 'originalName':
                        filepath = typedoc[key]
                        typedoc[key] = relpath(filepath)
            clean_paths(typedoc)
            with open(json_path, mode='w') as typedoc_json:
                dump(typedoc, typedoc_json)
        except OSError as exc:
            if exc.errno == ENOENT:
                raise SphinxError('%s was not found. Install it using "npm install -g typedoc".' % command.program)
            else:
                raise
            # typedoc emits a valid JSON file even if it finds no TS files in the dir:
    with open('mico-admin/ts/typedoc.json') as temp:
        return doclets.parse_typedoc(temp)


doclets.ANALYZERS['custom_typescript'] = analyze_typescript


def new_relpath(path, basedir):
    if basedir:
        path = join(basedir, path)
    return relpath(path, basedir)[1:] # grammar for path only allows for paths to start with './' and not '../'


doclets.relpath = new_relpath


# -- Project information -----------------------------------------------------

project = 'MICO Developer Documentation'
copyright = '2018, MICO Authors'
author = 'MICO Authors'

# The short X.Y version
version = '0.0.1'
# The full version, including alpha/beta/rc tags
release = '0.0.1'


# -- General configuration ---------------------------------------------------

# If your documentation needs a minimal Sphinx version, state it here.
#
# needs_sphinx = '1.0'

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx.ext.intersphinx',
    'sphinx.ext.ifconfig',
    'sphinx.ext.autosectionlabel',
    'sphinx.ext.todo',
    'sphinx.ext.imgmath',
    'sphinx.ext.graphviz',
    'sphinx_js',
    'javasphinx',
]

# Add any paths that contain templates here, relative to this directory.
templates_path = []

# Setup markdown parser:
source_parsers = {
    '.md': CommonMarkParser,
}

# The suffix(es) of source filenames.
# You can specify multiple suffix as a list of string:
#
source_suffix = ['.rst', '.md']
# source_suffix = '.rst'

# The master toctree document.
master_doc = 'index'

# The language for content autogenerated by Sphinx. Refer to documentation
# for a list of supported languages.
#
# This is also used if you do content translation via gettext catalogs.
# Usually you set "language" from the command line for these cases.
language = None

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path .
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store', 'README.md']

# The name of the Pygments (syntax highlighting) style to use.
pygments_style = 'sphinx'


# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
if on_rtd:
    html_theme = 'default'
else:
    html_theme = 'sphinx_rtd_theme'

# Theme options are theme-specific and customize the look and feel of a theme
# further.  For a list of options available for each theme, see the
# documentation.
#
# html_theme_options = {}

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = []

# Custom sidebar templates, must be a dictionary that maps document names
# to template names.
#
# The default sidebars (for documents that don't match any pattern) are
# defined by theme itself.  Builtin themes are using these templates by
# default: ``['localtoc.html', 'relations.html', 'sourcelink.html',
# 'searchbox.html']``.
#
# html_sidebars = {}


# -- Options for HTMLHelp output ---------------------------------------------

# Output file base name for HTML help builder.
htmlhelp_basename = 'MICODeveloperDocumentationdoc'


# -- Options for LaTeX output ------------------------------------------------

latex_elements = {
    # The paper size ('letterpaper' or 'a4paper').
    #
    # 'papersize': 'letterpaper',

    # The font size ('10pt', '11pt' or '12pt').
    #
    # 'pointsize': '10pt',

    # Additional stuff for the LaTeX preamble.
    #
    # 'preamble': '',

    # Latex figure (float) alignment
    #
    # 'figure_align': 'htbp',
}

# Grouping the document tree into LaTeX files. List of tuples
# (source start file, target name, title,
#  author, documentclass [howto, manual, or own class]).
latex_documents = [
    (master_doc, 'MICODeveloperDocumentation.tex', 'MICO Developer Documentation',
     'MICO Authors', 'manual'),
]


# -- Options for manual page output ------------------------------------------

# One entry per manual page. List of tuples
# (source start file, name, description, authors, manual section).
man_pages = [
    (master_doc, 'micodeveloperdocumentation', 'MICO Developer Documentation',
     [author], 1)
]


# -- Options for Texinfo output ----------------------------------------------

# Grouping the document tree into Texinfo files. List of tuples
# (source start file, target name, title, author,
#  dir menu entry, description, category)
texinfo_documents = [
    (master_doc, 'MICODeveloperDocumentation', 'MICO Developer Documentation',
     author, 'MICODeveloperDocumentation', 'One line description of project.',
     'Miscellaneous'),
]


# -- Extension configuration -------------------------------------------------

# -- Options for intersphinx extension ---------------------------------------

# Example configuration for intersphinx: refer to the Python standard library.
intersphinx_mapping = {
    'python': ('https://docs.python.org/3/', None),
    'mico': ('https://mico-docs.readthedocs.io/en/latest/', None),
}

# -- Options for todo extension ----------------------------------------------

# If true, `todo` and `todoList` produce output, else they produce nothing.
todo_include_todos = not on_rtd
todo_emit_warnings = not on_rtd

# -- Options for recommonmark ------------------------------------------------
autosectionlabel_prefix_document = True


# app setup hook
def setup(app):
    app.add_config_value('recommonmark_config', {
        'auto_toc_tree': True,
        'enable_auto_doc_ref': True,
        'enable_eval_rst': True,
        'enable_math': True,
        'enable_inline_math': True,
    }, True)
    app.add_config_value('on_rtd', on_rtd, 'env')
    app.add_transform(AutoStructify)

# -- Options for jsdoc -------------------------------------------------------
js_language = 'custom_typescript'
root_for_relative_js_paths = '.'
js_source_path = ['../mico-admin', '../mico-grapheditor']

# -- Options for javasphinx --------------------------------------------------
javadoc_url_map = {
    #'com.netflix.curator' : ('http://netflix.github.com/curator/doc', 'javadoc8'),
}
