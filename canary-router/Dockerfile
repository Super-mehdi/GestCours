# Routeur Canary : remplace l'AWS ALB (Weighted Target Groups) sur Render
FROM nginx:1.27-alpine
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
