Grapheditor
===========


Component Attributes
--------------------

.. describe:: nodes

   A json list of ``Node`` objects. All ``'`` characters will be replaced with ``"`` before parsing the json!

.. describe:: edges

   A json list of ``Edge`` objects. All ``'`` characters will be replaced with ``"`` before parsing the json!

.. describe:: mode

   The interaction mode of the graph.

   *  ``display`` allow no user interaction with the graph except selecting nodes.
   *  ``layout`` allow free layout manipulation by user.
   *  ``link`` allow creating and destroying links by user.
   *  ``select`` allow only selecting and deselecting nodes.

.. describe:: zoom

   Controls pan and zoom behaviour of graph.

   *  ``none`` graph will not pan/zoom at all.
   *  ``manual`` allow free pan/zoom by user.
   *  ``automatic`` graph will pan/zoom after (re-)render to show all nodes.
   *  ``both`` both manual and automatic.



Example Usage
^^^^^^^^^^^^^

.. code-block:: html

    <network-graph
            nodes="[{'id': 1, 'title': 'hello world', 'type': 'REST', 'x': 0, 'y': 0}, {'id': 2, 'title': 'HI2', 'type': 'gRPC', 'x': 150, 'y': 100}]"
            edges="[{'source': 1, 'target': 2}]"
            mode="layout"
            zoom="both">
    </network-graph>




Component Styling
-----------------

It is possible to inject styles and node templates into the component via ``template`` tags.

Style templates need to have the attribute ``template-type="style"`` and contain one ``<style>`` tag.

Node templates need to have the attribute ``template-type="node"`` and should have a unique id that corresponds to a specific node type.

.. todo:: document available classes

.. todo:: document text injection behaviour

Example Usage
^^^^^^^^^^^^^

.. code-block:: html

    <network-graph>
        <template template-type="style">
            <style>
                .node {fill: aqua;}
                .text {fill: black;}
                .node.hovered {fill: red;}
                .node.selected {fill: green; content:attr(class)}
                .edge.highlight-outgoing {stroke: red;}
                .edge.highlight-incoming {stroke: green;}
            </style>
        </template>
        <template id="node" template-type="node">
            <rect width="100" height="60" x="-50" y="-30"></rect>
            <text class="title text" data-content="title" data-click="title" x="-40" y="-10"></text>
            <text class="text" data-content="type" x="-40" y="10"></text>
        </template>
    </network-graph>




Component Events
----------------

The graph component uses `custom events <https://developer.mozilla.org/en-US/docs/Web/Guide/Events/Creating_and_triggering_events>`_. Custom event data can be accessed via the ``detail`` attribute.

.. warning::

    Custom events get dispatched synchronously!

.. describe:: modechange

    Fired after the interaction mode changed.

    **Example** ``detail``

    .. code-block:: ts

        {
            "oldMode": "layout",
            "newMode": "select"
        }

.. describe:: zoommodechange

    Fired after the zoom mode changed.

    **Example** ``detail``

    .. code-block:: ts

        {
            "oldMode": "none",
            "newMode": "both"
        }

.. describe:: selection

    Fired when a user (de-)selects a node.

    **Example** ``detail``

    .. code-block:: ts

        {
            "selection": new Set<number|string>([1, 2, 5])
        }

.. describe:: nodeclick

    Fired when a user clicks on a node. The ``key`` can be used to create :ref:`custom buttons <example-events>`.

    Use ``event.preventDefault()`` to prevent standard graph behaviour.

    **Example** ``detail``

    .. code-block:: ts

        {
            "sourceEvent": {},
            "node": {
                "id": 1,
                "x": 0,
                "y": 0
            },
            "key": "close"
        }


.. describe:: nodeenter

    Fired when a user enters a node with a mouse or pointer device.

    **Example** ``detail``

    .. code-block:: ts

        {
            "sourceEvent": {},
            "node": {
                "id": 1,
                "x": 0,
                "y": 0
            }
        }

.. describe:: nodeleave

    Fired when a user leaves a node with a mouse or pointer device.

    **Example** ``detail``

    .. code-block:: ts

        {
            "sourceEvent": {},
            "node": {
                "id": 1,
                "x": 0,
                "y": 0
            }
        }

.. describe:: nodepositionchange

    Fired when a node gets new coordinates.

    **Example** ``detail``

    .. code-block:: ts

        {
            "node": {
                "id": 1,
                "x": 0,
                "y": 0
            }
        }

.. describe:: nodeadd

    Fired when a node gets added to the graph.

    Use ``event.preventDefault()`` to prevent standard graph behaviour.

    **Example** ``detail``

    .. code-block:: ts

        {
            "node": {
                "id": 1,
                "x": 0,
                "y": 0
            }
        }

.. describe:: noderemove

    Fired when a node gets removed from the graph.

    Use ``event.preventDefault()`` to prevent standard graph behaviour.

    **Example** ``detail``

    .. code-block:: ts

        {
            "node": {
                "id": 1,
                "x": 0,
                "y": 0
            }
        }
.. describe:: edgeclick

    Fired when a user clicks on a edge.

    Use ``event.preventDefault()`` to prevent standard graph behaviour.

    **Example** ``detail``

    .. code-block:: ts

        {
            "sourceEvent": {},
            "edge": {
                "source": 1,
                "target": 2
            }
        }

.. describe:: edgeadd

    Fired when an edge gets added to the graph.

    Use ``event.preventDefault()`` to prevent standard graph behaviour.

    **Example** ``detail``

    .. code-block:: ts

        {
            "edge": {
                "source": 1,
                "target": 2
            }
        }

.. describe:: edgeremove

    Fired when an edge gets removed from the graph.

    Use ``event.preventDefault()`` to prevent standard graph behaviour.

    **Example** ``detail``

    .. code-block:: ts

        {
            "edge": {
                "source": 1,
                "target": 2
            }
        }



.. _example-events:

Example Usage
^^^^^^^^^^^^^

This example uses a node template where one part has the ``data-click="remove"`` attribute. This attribute is used in the event to populate the ``key`` attribute.

.. code-block:: html

    <network-graph>
        <template id="node" template-type="node">
            <rect width="100" height="60" x="-50" y="-30"></rect>
            <text class="text" data-click="remove" x="-40" y="-10">remove</text>
        </template>
    </network-graph>
    <script>
        var graph = document.querySelector('network-graph');
        graph.addEventListener('nodeclick', function test(event) {
            console.log(event.type, event.detail);
            if (event.detail.key === 'remove') {
                event.preventDefault();
            }
        });
    </script>


Public API
----------

.. js:autoclass:: GraphEditor
   :members: nodeList, edgeList, mode, zoomMode, setMode, setZoomMode, setNodes, setEdges, addNode, removeNode, addEdge, removeEdge, completeRender, zoomToBoundingBox, onCreateDraggedEdge, onDraggedEdgeTargetChange, onDropDraggedEdge

