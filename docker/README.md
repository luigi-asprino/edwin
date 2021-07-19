# Docker

To run Edwin in a docker container proceeds as follows:

1. Build docker image ``edwin_test``  (from the directory named ``docker``).

```
 docker build test -t edwin_test
```
2. Copy the RDF file that you want to analyse in ``vol/`` (e.g. ``lov.nq.gz``).

3. Edit configuration file (``conf.properties``) in ``vol/``.

4. Run run docker image ``edwin_test``

```
 docker run --volume=$(pwd)/vol:/opt/data edwin_test
 ```
 The resulting ESG together with its statistics can be found in ``vol/${esgName}`` (where ``${esgName}`` is the name of the Equivalence Set Graph provided in the configuration file ``vol/conf.properties``.
