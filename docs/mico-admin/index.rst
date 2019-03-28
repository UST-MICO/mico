MICO-Admin UI
=============

The MICO-Admin UI uses the `Angular <https://v7.angular.io>`_ framework (v7) with the `Angular Material <https://material.angular.io>`_ component library.

local testing
-------------

To run the frontend locally first install all dependencys and the start the development server.

.. sourcecode:: bash

    cd mico-admin
    npm install
    npm run start

Then open http://localhost:4200 in your browser.

.. hint::

    The frontend (in development mode) expects the backend at http://localhost:8080.
    You can use :program:`kubectl` to forward traffic to a backend running in a cluster.


Documentation of Core Components
--------------------------------

.. toctree::
   ts/index


Mockups
-------

* :ref:`mockup-initial-sketch-dashboard`
* :ref:`mockup-initial-sketch-appdetail-overview`
* :ref:`mockup-initial-sketch-appdetail-deploy-status`
* :ref:`mockup-initial-sketch-appdetail-deploy-settings`
* :ref:`mockup-initial-sketch-servicedetail-overview`
* :ref:`mockup-initial-sketch-servicedetail-deploy-status`

Design Decisions
----------------

* :doc:`adr/0006-ui-framework`
* :doc:`adr/0008-browser-compatibility`
* :doc:`adr/0009-features-first`
* :doc:`adr/0011-json+hal`
* :doc:`adr/0012-winery-topology-modeler`
