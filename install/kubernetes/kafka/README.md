# Kubernetes Kafka

This setup is based on community GitHub repository [Yolean/kubernetes-kafka](https://github.com/Yolean/kubernetes-kafka).

**Modifications**:
* `/variants/aks-managed/kustomization.yaml`:

  Use `../dev-small` instead of `../scale-3-5` of the base

Deploy Kafka with its dependencies:
```bash
kubectl apply -f 00-namespace.yml && kubectl apply -k ./variants/aks-managed/
```
