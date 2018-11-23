Mico Form Component
===================


Component Usage
---------------

.. describe:: @Input() modelUrl: string

   A string identifying the jsonschema model to use. Example: ``local/servicePOST``

.. describe:: @Input() filter: string[] = []

   A list of properties to filter by.

.. describe:: @Input() isBlacklist: boolean = false

   If property filter is whitelist or blacklist.

.. describe:: @Input() debug: boolean = false

   If form renders debug data.

.. describe:: @Output() valid: EventEmitter<boolean>

   If form is valid.

.. describe:: @Output() data: EventEmitter<any>

   Form data as js object.


Example Usage
^^^^^^^^^^^^^

.. code-block:: html+ng2

   <mico-form [modelUrl]="'local/servicePOST'" [filter]="['name']" (data)="data = $event"></mico-form>


Class
-----

.. js:autoclass:: MicoFormComponent
   :members:
   :private-members:
