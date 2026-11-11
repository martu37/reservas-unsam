.PHONY: start restart esperar-backend stop logs reset db

VERDE := \033[1;32m
AZUL  := \033[1;34m
ROJO  := \033[1;31m
FIN   := \033[0m

# Levanta todo en Docker (bases, backend y frontend) y devuelve la terminal.
# --build recompila backend y frontend si cambió el código.
start:
	@printf "$(AZUL)==> Construyendo y levantando contenedores (la primera vez tarda)...$(FIN)\n"
	@docker compose up -d --build
	@$(MAKE) --no-print-directory esperar-backend

# Después de editar código: recompila y reinicia solo backend y frontend.
# Las bases no se tocan (los datos quedan como estaban).
restart:
	@printf "$(AZUL)==> Recompilando y reiniciando backend y frontend...$(FIN)\n"
	@docker compose up -d --build backend frontend
	@$(MAKE) --no-print-directory esperar-backend

esperar-backend:
	@printf "$(AZUL)==> Esperando al backend$(FIN)"; \
	until curl -s -o /dev/null http://localhost:8080; do \
		if [ "$$(docker inspect -f '{{.State.Running}}' reservas-backend 2>/dev/null)" != "true" ]; then \
			printf "\n$(ROJO)==> El backend se cayó. Últimas líneas del log:$(FIN)\n"; \
			docker compose logs --tail 25 backend; exit 1; \
		fi; \
		printf "."; sleep 2; \
	done; \
	printf "\n"
	@printf "\n$(VERDE)Todo levantado. Abrí http://localhost:5173$(FIN)\n"
	@printf "  Ver logs:   make logs\n"
	@printf "  Bajar todo: make stop\n"

# Logs de backend y frontend en vivo (Ctrl+C para salir, la app sigue)
logs:
	docker compose logs -f --tail 30 backend frontend

stop:
	@docker compose down
	@printf "$(VERDE)==> Todo apagado$(FIN)\n"

# Borra los datos y recarga los scripts de database/ (también borra MongoDB)
reset:
	docker compose down -v
	@$(MAKE) --no-print-directory start

# Solo las bases, para desarrollar backend/frontend fuera de Docker
db:
	docker compose up -d postgres mongo
