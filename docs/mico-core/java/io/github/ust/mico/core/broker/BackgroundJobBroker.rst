.. java:import:: io.github.ust.mico.core.exception MicoApplicationNotFoundException

.. java:import:: io.github.ust.mico.core.model MicoServiceBackgroundJob.Status

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoBackgroundJobRepository

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.lang Nullable

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.util StringUtils

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.concurrent CompletableFuture

.. java:import:: java.util.stream Collectors

BackgroundJobBroker
===================

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class BackgroundJobBroker

   Broker to operate with jobs.

Constructors
------------
BackgroundJobBroker
^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public BackgroundJobBroker(MicoBackgroundJobRepository jobRepository, MicoApplicationRepository applicationRepository)
   :outertype: BackgroundJobBroker

Methods
-------
deleteJob
^^^^^^^^^

.. java:method:: public void deleteJob(String id)
   :outertype: BackgroundJobBroker

   Deletes a job in the database. If the future is still running, it will be cancelled.

   :param id: the id of the job.

getAllJobs
^^^^^^^^^^

.. java:method:: public List<MicoServiceBackgroundJob> getAllJobs()
   :outertype: BackgroundJobBroker

   Retrieves all jobs saved in database.

   :return: a \ :java:ref:`List`\  of \ :java:ref:`MicoServiceBackgroundJob`\ .

getJobById
^^^^^^^^^^

.. java:method:: public Optional<MicoServiceBackgroundJob> getJobById(String id)
   :outertype: BackgroundJobBroker

   Retrieves a job by id.

   :param id: the id of the job.
   :return: a \ :java:ref:`MicoServiceBackgroundJob`\ .

getJobByMicoService
^^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<MicoServiceBackgroundJob> getJobByMicoService(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type)
   :outertype: BackgroundJobBroker

   Return a \ ``MicoServiceBackgroundJob``\  for a given \ ``MicoService``\  and \ ``MicoServiceBackgroundJob.Type``\ .

   :param micoServiceShortName: the short name of a \ :java:ref:`MicoService`\
   :param micoServiceVersion: the version of a \ :java:ref:`MicoService`\
   :param type: the \ :java:ref:`MicoServiceBackgroundJob.Type`\
   :return: the optional Job. Is empty if no Job exist for the given \ :java:ref:`MicoService`\

getJobStatusByApplicationShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationJobStatus getJobStatusByApplicationShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException
   :outertype: BackgroundJobBroker

   Retrieves the job status of a \ ``MicoApplication``\ .

   :param shortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param version: the version of the \ :java:ref:`MicoApplication`\ .
   :return: the \ :java:ref:`MicoApplicationJobStatus`\  with the status and jobs.

saveFutureOfJob
^^^^^^^^^^^^^^^

.. java:method:: public void saveFutureOfJob(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type, CompletableFuture<?> future)
   :outertype: BackgroundJobBroker

   Saves a future of a job to the database.

   :param micoServiceShortName: the short name of a \ :java:ref:`MicoService`\
   :param micoServiceVersion: the version of a \ :java:ref:`MicoService`\
   :param future: the future as a \ :java:ref:`CompletableFuture`\
   :param type: the \ :java:ref:`MicoServiceBackgroundJob.Type`\

saveJob
^^^^^^^

.. java:method:: public MicoServiceBackgroundJob saveJob(MicoServiceBackgroundJob job)
   :outertype: BackgroundJobBroker

   Save a job to the database.

   :param job: the \ :java:ref:`MicoServiceBackgroundJob`\
   :return: the saved \ :java:ref:`MicoServiceBackgroundJob`\

saveNewStatus
^^^^^^^^^^^^^

.. java:method:: public void saveNewStatus(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type, MicoServiceBackgroundJob.Status newStatus)
   :outertype: BackgroundJobBroker

   Saves a new status of a job to the database.

   :param micoServiceShortName: the short name of a \ :java:ref:`MicoService`\
   :param micoServiceVersion: the version of a \ :java:ref:`MicoService`\
   :param type: the \ :java:ref:`MicoServiceBackgroundJob.Type`\
   :param newStatus: the new \ :java:ref:`MicoServiceBackgroundJob.Status`\

saveNewStatus
^^^^^^^^^^^^^

.. java:method:: public void saveNewStatus(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundJob.Type type, MicoServiceBackgroundJob.Status newStatus, String errorMessage)
   :outertype: BackgroundJobBroker

   Saves a new status of a job to the database.

   :param micoServiceShortName: the short name of a \ :java:ref:`MicoService`\
   :param micoServiceVersion: the version of a \ :java:ref:`MicoService`\
   :param type: the \ :java:ref:`MicoServiceBackgroundJob.Type`\
   :param newStatus: the new \ :java:ref:`MicoServiceBackgroundJob.Status`\
   :param errorMessage: the optional error message if the job has failed

