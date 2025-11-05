#!/bin/bash
# Script de configuration d'un cluster Kubernetes local avec Minikube
# Utilisez ce script si vous n'avez pas de cluster Kubernetes configuré

set -e

echo "=========================================="
echo "🚀 Configuration d'un Cluster Local"
echo "=========================================="
echo ""

# Vérifier si minikube est installé
echo "1. Vérification de Minikube..."
if ! command -v minikube &> /dev/null; then
    echo "⬇️  Installation de Minikube..."

    # Détecter l'OS
    OS="$(uname -s)"
    case "${OS}" in
        Linux*)
            curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
            sudo install minikube-linux-amd64 /usr/local/bin/minikube
            rm minikube-linux-amd64
            ;;
        Darwin*)
            brew install minikube
            ;;
        *)
            echo "❌ OS non supporté pour l'installation automatique"
            echo "   Installez Minikube manuellement : https://minikube.sigs.k8s.io/docs/start/"
            exit 1
            ;;
    esac
    echo "   ✅ Minikube installé"
else
    echo "   ✅ Minikube déjà installé : $(minikube version --short)"
fi
echo ""

# Vérifier si kubectl est installé
echo "2. Vérification de kubectl..."
if ! command -v kubectl &> /dev/null; then
    echo "⬇️  Installation de kubectl..."

    # Détecter l'OS
    OS="$(uname -s)"
    case "${OS}" in
        Linux*)
            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
            sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
            rm kubectl
            ;;
        Darwin*)
            brew install kubectl
            ;;
        *)
            echo "❌ OS non supporté pour l'installation automatique"
            echo "   Installez kubectl manuellement : https://kubernetes.io/docs/tasks/tools/"
            exit 1
            ;;
    esac
    echo "   ✅ kubectl installé"
else
    echo "   ✅ kubectl déjà installé : $(kubectl version --client --short 2>/dev/null || kubectl version --client)"
fi
echo ""

# Démarrer Minikube
echo "3. Démarrage de Minikube..."
if minikube status &> /dev/null; then
    echo "   ℹ️  Minikube est déjà en cours d'exécution"
else
    echo "   🚀 Démarrage du cluster..."
    minikube start --driver=docker --cpus=2 --memory=4096
    echo "   ✅ Minikube démarré"
fi
echo ""

# Activer les addons nécessaires
echo "4. Configuration des addons..."
echo "   📦 Activation de ingress..."
minikube addons enable ingress
echo "   📦 Activation de metrics-server..."
minikube addons enable metrics-server
echo "   ✅ Addons configurés"
echo ""

# Créer les namespaces
echo "5. Création des namespaces..."
kubectl create namespace soa-integration --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace soa-production --dry-run=client -o yaml | kubectl apply -f -
echo "   ✅ Namespaces créés"
echo ""

# Vérifier le cluster
echo "6. Vérification du cluster..."
kubectl cluster-info
echo ""
kubectl get nodes
echo ""

echo "=========================================="
echo "✅ Cluster Local Configuré"
echo "=========================================="
echo ""
echo "Le cluster Kubernetes local est prêt!"
echo ""
echo "Commandes utiles:"
echo "  - État du cluster    : minikube status"
echo "  - Arrêter le cluster : minikube stop"
echo "  - Supprimer le cluster : minikube delete"
echo "  - Dashboard          : minikube dashboard"
echo "  - Tunnel pour ingress: minikube tunnel (à exécuter dans un terminal séparé)"
echo ""
#!/bin/bash
# Script de vérification de la configuration Kubernetes
# Ce script vérifie que kubectl est correctement configuré et que le cluster est accessible

set -e

echo "=========================================="
echo "🔍 Vérification du Cluster Kubernetes"
echo "=========================================="
echo ""

# Vérifier si kubectl est installé
echo "1. Vérification de kubectl..."
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl n'est pas installé"
    echo "   Installez kubectl : https://kubernetes.io/docs/tasks/tools/"
    exit 1
fi
echo "   ✅ kubectl installé : $(kubectl version --client --short 2>/dev/null || kubectl version --client)"
echo ""

# Vérifier la configuration kubeconfig
echo "2. Vérification de la configuration kubeconfig..."
if [ ! -f "$HOME/.kube/config" ]; then
    echo "❌ Fichier kubeconfig non trouvé ($HOME/.kube/config)"
    echo "   Options:"
    echo "   - Configurez un cluster Kubernetes (minikube, kind, k3s, etc.)"
    echo "   - Ou définissez la variable KUBECONFIG"
    exit 1
fi
echo "   ✅ Fichier kubeconfig trouvé"
echo ""

# Vérifier la connexion au cluster
echo "3. Vérification de la connexion au cluster..."
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Impossible de se connecter au cluster Kubernetes"
    echo ""
    echo "Détails de l'erreur:"
    kubectl cluster-info 2>&1 || true
    echo ""
    echo "Solutions possibles:"
    echo "   1. Démarrez minikube : minikube start"
    echo "   2. Vérifiez que votre cluster est en cours d'exécution"
    echo "   3. Vérifiez votre configuration kubeconfig"
    exit 1
fi
echo "   ✅ Connexion au cluster réussie"
echo ""

# Afficher les informations du cluster
echo "4. Informations du cluster:"
kubectl cluster-info
echo ""

# Vérifier les nodes
echo "5. Nodes disponibles:"
kubectl get nodes
echo ""

# Vérifier les namespaces requis
echo "6. Vérification des namespaces requis..."
NAMESPACES=("soa-integration" "soa-production")
for ns in "${NAMESPACES[@]}"; do
    if kubectl get namespace "$ns" &> /dev/null; then
        echo "   ✅ Namespace $ns existe"
    else
        echo "   ⚠️  Namespace $ns n'existe pas (sera créé lors du déploiement)"
    fi
done
echo ""

echo "=========================================="
echo "✅ Vérification terminée avec succès"
echo "=========================================="
echo ""
echo "Le cluster Kubernetes est prêt pour le déploiement."
echo ""

