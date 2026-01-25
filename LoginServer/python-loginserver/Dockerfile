FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

COPY pyproject.toml /app/pyproject.toml
COPY loginserver /app/loginserver
COPY config /app/config

RUN pip install --upgrade pip && pip install .

EXPOSE 53699 9102

CMD ["python", "-m", "loginserver.server", "--config", "config/loginserver.yaml"]
