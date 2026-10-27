#!/bin/sh
# Génère la config Nginx à partir de :
#   STABLE_HOST / CANARY_HOST : hôtes Render (ex. app-stable-xxxx.onrender.com)
#   CANARY_WEIGHT             : % du trafic envoyé au canary (0 = rollback réseau)
set -e
: "${STABLE_HOST:?STABLE_HOST manquant}"
: "${CANARY_HOST:?CANARY_HOST manquant}"
CANARY_WEIGHT="${CANARY_WEIGHT:-10}"
PORT="${PORT:-10000}"

if [ "$CANARY_WEIGHT" -le 0 ]; then
  SPLIT="* stable;"
elif [ "$CANARY_WEIGHT" -ge 100 ]; then
  SPLIT="* canary;"
else
  SPLIT="${CANARY_WEIGHT}% canary; * stable;"
fi

cat > /etc/nginx/conf.d/default.conf <<CONF
resolver 1.1.1.1 8.8.8.8 valid=30s ipv6=off;

split_clients "\$request_id" \$target {
    $SPLIT
}

map \$target \$upstream_host {
    canary $CANARY_HOST;
    default $STABLE_HOST;
}

log_format canary '\$time_iso8601 \$request -> \$target (\$upstream_host) \$status';

server {
    listen $PORT;
    access_log /dev/stdout canary;

    location = /router/health {
        return 200 "canary-router ok (canary weight: ${CANARY_WEIGHT}%)\n";
    }

    location / {
        proxy_pass https://\$upstream_host;
        proxy_set_header Host \$upstream_host;
        proxy_ssl_server_name on;
        proxy_ssl_name \$upstream_host;
        proxy_connect_timeout 5s;
        proxy_read_timeout 60s;
        add_header X-Canary-Target \$target always;
    }
}
CONF

echo "canary-router: stable=$STABLE_HOST canary=$CANARY_HOST weight=${CANARY_WEIGHT}%"
exec nginx -g 'daemon off;'
