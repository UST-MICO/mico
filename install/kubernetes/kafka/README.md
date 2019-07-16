# Kubernetes Kafka

This setup is based on community GitHub repository [Yolean/kubernetes-kafka](https://github.com/Yolean/kubernetes-kafka).

Deploy Kafka with its dependencies:
```bash
kubectl apply -f 00-namespace.yml && kubectl apply -k ./variants/dev-small/
```

Deploy kafkacat to Kubernetes:
```bash
kubectl apply -f 01-test-namespace.yml && kubectl apply -f ./kafka/test/kafkacat.yml
```
